from io import BytesIO
from typing import List

from PIL import Image
from pypdf import PdfReader, PdfWriter

from app.core.logging import AppLogger

logger = AppLogger().get_logger()

PDF_MAGIC_BYTES = b"%PDF-"


def _is_pdf(file_bytes: bytes) -> bool:
    return file_bytes.lstrip()[:len(PDF_MAGIC_BYTES)] == PDF_MAGIC_BYTES


def _image_to_pdf_bytes(file_bytes: bytes) -> bytes:
    image = Image.open(BytesIO(file_bytes))
    if image.mode != "RGB":
        image = image.convert("RGB")
    output = BytesIO()
    image.save(output, format="PDF")
    return output.getvalue()


def _to_pdf_bytes(file_bytes: bytes) -> bytes:
    """Return `file_bytes` as-is if already a PDF, otherwise convert the image to a single-page PDF."""
    if _is_pdf(file_bytes):
        return file_bytes
    try:
        return _image_to_pdf_bytes(file_bytes)
    except Exception as e:
        raise ValueError(f"File is neither a valid PDF nor a supported image: {e}")


def append_documents_to_pdf(parent_pdf_bytes: bytes, document_bytes_list: List[bytes]) -> bytes:
    """
    Appends each document in `document_bytes_list` (PDF or image bytes, in order) to the
    end of `parent_pdf_bytes` and returns the merged PDF as bytes.
    """
    writer = PdfWriter()
    writer.append(PdfReader(BytesIO(parent_pdf_bytes)))

    for index, document_bytes in enumerate(document_bytes_list):
        try:
            pdf_bytes = _to_pdf_bytes(document_bytes)
            writer.append(PdfReader(BytesIO(pdf_bytes)))
        except Exception as e:
            logger.error(f"Error appending document at index {index} to parent PDF: {e}", exc_info=True)
            raise

    merged = BytesIO()
    writer.write(merged)
    writer.close()
    return merged.getvalue()
