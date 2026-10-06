import os

from dotenv import load_dotenv
from fastapi import APIRouter, HTTPException

from app.core.logging import AppLogger
from app.schemas.document import DocumentAppendRequest, DocumentAppendResponse
from app.utils.filestore_service_client import FileStoreServiceClient
from app.utils.pdf_utils import append_documents_to_pdf

load_dotenv()
filestore_service_url = os.getenv("FILESTORE_SERVICE_URL")

router = APIRouter()
logger = AppLogger().get_logger()


@router.post('/append',
             response_model=DocumentAppendResponse,
             summary='Append one or more documents (image or PDF) to the end of a parent PDF',
             response_description="Returns the fileStoreId of the merged PDF")
async def append_documents(payload: DocumentAppendRequest):
    logger.trace(f"Starting document append: parentFileStoreId={payload.parentFileStoreId}, "
                 f"documentCount={len(payload.documents)}")

    if not filestore_service_url:
        raise HTTPException(status_code=500, detail="Filestore service is not configured")

    if not payload.documents:
        raise HTTPException(status_code=400, detail="At least one document is required to append")

    filestore_client = FileStoreServiceClient(filestore_service_url)

    try:
        parent_bytes, _ = filestore_client.download_file(payload.tenantId, payload.parentFileStoreId)
    except Exception as e:
        logger.error(f"Error downloading parent PDF fileStoreId={payload.parentFileStoreId}: {e}", exc_info=True)
        raise HTTPException(status_code=502, detail=f"Failed to fetch parent PDF: {str(e)}")

    document_bytes_list = []
    for document in payload.documents:
        if not document.fileStoreId:
            raise HTTPException(status_code=400, detail="Each document must have a fileStoreId")
        try:
            document_bytes, _ = filestore_client.download_file(payload.tenantId, document.fileStoreId)
            document_bytes_list.append(document_bytes)
        except Exception as e:
            logger.error(f"Error downloading document fileStoreId={document.fileStoreId}: {e}", exc_info=True)
            raise HTTPException(
                status_code=502, detail=f"Failed to fetch document {document.fileStoreId}: {str(e)}"
            )

    try:
        merged_pdf_bytes = append_documents_to_pdf(parent_bytes, document_bytes_list)
    except ValueError as e:
        logger.error(f"Invalid document content while merging: {e}", exc_info=True)
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.error(f"Error merging documents into parent PDF: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Failed to merge documents: {str(e)}")

    try:
        merged_file_store_id = filestore_client.upload_file(
            file_bytes=merged_pdf_bytes,
            file_name=f"{payload.parentFileStoreId}_merged.pdf",
            tenant_id=payload.tenantId,
            module=payload.module,
        )
    except Exception as e:
        logger.error(f"Error uploading merged PDF to filestore: {e}", exc_info=True)
        raise HTTPException(status_code=502, detail=f"Failed to upload merged PDF: {str(e)}")

    logger.info(f"Document append completed: parentFileStoreId={payload.parentFileStoreId}, "
                f"mergedFileStoreId={merged_file_store_id}, documentCount={len(payload.documents)}")

    return DocumentAppendResponse(
        fileStoreId=merged_file_store_id,
        tenantId=payload.tenantId,
        appendedDocumentCount=len(payload.documents),
    )
