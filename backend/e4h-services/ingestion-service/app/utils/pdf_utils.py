from io import BytesIO
from typing import List, Optional

from PIL import Image
from pypdf import PageObject, PdfReader, PdfWriter, Transformation
from pypdf.generic import DecodedStreamObject, DictionaryObject, NameObject

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


# Heading band added above a document's first page, sized relative to the page width so it reads
# the same on an A4 PDF page and on a full-resolution photo page.
HEADING_FONT_RATIO = 0.035
HEADING_BAND_RATIO = 0.07
HEADING_MARGIN_RATIO = 0.05
HEADING_MIN_FONT_SIZE = 14


def _escape_pdf_text(text: str) -> bytes:
    """Encode `text` as a PDF literal string body (Helvetica uses Latin-1; other chars become '?')."""
    encoded = text.encode("latin-1", errors="replace")
    return encoded.replace(b"\\", b"\\\\").replace(b"(", b"\\(").replace(b")", b"\\)")


def _with_heading(page: PageObject, heading: str) -> PageObject:
    """
    Returns a copy of `page` with a band added on top holding `heading` in Helvetica-Bold.
    The original content is kept unscaled below the band, so nothing on the page is covered.
    """
    page.transfer_rotation_to_content()
    box = page.mediabox
    width, height = float(box.width), float(box.height)

    font_size = max(HEADING_MIN_FONT_SIZE, width * HEADING_FONT_RATIO)
    band = max(font_size * 2, width * HEADING_BAND_RATIO)
    margin = width * HEADING_MARGIN_RATIO

    new_page = PageObject.create_blank_page(width=width, height=height + band)
    new_page.merge_transformed_page(page, Transformation().translate(-float(box.left), -float(box.bottom)))

    heading_page = PageObject.create_blank_page(width=width, height=height + band)
    font = DictionaryObject({
        NameObject("/Type"): NameObject("/Font"),
        NameObject("/Subtype"): NameObject("/Type1"),
        NameObject("/BaseFont"): NameObject("/Helvetica-Bold"),
        NameObject("/Encoding"): NameObject("/WinAnsiEncoding"),
    })
    heading_page[NameObject("/Resources")] = DictionaryObject({
        NameObject("/Font"): DictionaryObject({NameObject("/FHeading"): font}),
    })
    baseline = height + (band - font_size) / 2 + font_size * 0.2
    content = DecodedStreamObject()
    content.set_data(
        b"BT /FHeading %.2f Tf %.2f %.2f Td (" % (font_size, margin, baseline)
        + _escape_pdf_text(heading)
        + b") Tj ET"
    )
    heading_page[NameObject("/Contents")] = content
    new_page.merge_page(heading_page)
    return new_page


def append_documents_to_pdf(parent_pdf_bytes: bytes, document_bytes_list: List[bytes],
                            headings: Optional[List[Optional[str]]] = None) -> bytes:
    """
    Appends each document in `document_bytes_list` (PDF or image bytes, in order) to the
    end of `parent_pdf_bytes` and returns the merged PDF as bytes.

    `headings`, when given, is parallel to `document_bytes_list`: a non-blank entry is drawn as a
    title at the top of that document's first page.
    """
    writer = PdfWriter()
    writer.append(PdfReader(BytesIO(parent_pdf_bytes)))

    for index, document_bytes in enumerate(document_bytes_list):
        heading = headings[index] if headings and index < len(headings) else None
        try:
            reader = PdfReader(BytesIO(_to_pdf_bytes(document_bytes)))
            for page_index, page in enumerate(reader.pages):
                if page_index == 0 and heading and heading.strip():
                    page = _with_heading(page, heading.strip())
                writer.add_page(page)
        except Exception as e:
            logger.error(f"Error appending document at index {index} to parent PDF: {e}", exc_info=True)
            raise

    merged = BytesIO()
    writer.write(merged)
    writer.close()
    return merged.getvalue()
