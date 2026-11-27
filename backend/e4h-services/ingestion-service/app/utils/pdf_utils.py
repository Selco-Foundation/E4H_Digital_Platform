from io import BytesIO
from typing import List, Optional

from PIL import Image
from pypdf import PageObject, PdfReader, PdfWriter
from pypdf.generic import ContentStream, DecodedStreamObject, DictionaryObject, NameObject, RectangleObject

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


def _add_heading(page: PageObject, heading: str) -> None:
    """
    Grows `page` upwards by a band and draws `heading` in it in Helvetica-Bold. The original content
    keeps its size and position, so nothing on the page is covered.

    `page` must already belong to a PdfWriter (the one returned by `writer.add_page`): since pypdf 7
    a page's content can only be rewritten once it is attached to a writer.
    """
    page.transfer_rotation_to_content()
    # The visible area is the CropBox (pypdf falls back to the MediaBox when there is none).
    box = page.cropbox
    left, bottom = float(box.left), float(box.bottom)
    width, height = float(box.width), float(box.height)

    font_size = max(HEADING_MIN_FONT_SIZE, width * HEADING_FONT_RATIO)
    band = max(font_size * 2, width * HEADING_BAND_RATIO)
    margin = width * HEADING_MARGIN_RATIO

    # Content outside the visible area was hidden by the page boxes; clip it so it does not
    # reappear in the band once the page grows.
    contents = page.get_contents()
    if contents is not None:
        clipped = ContentStream(None, None)
        clipped.set_data(b"q %.4f %.4f %.4f %.4f re W n\n" % (left, bottom, width, height)
                         + contents.get_data() + b"\nQ\n")
        page.replace_contents(clipped)

    page.mediabox = RectangleObject([left, bottom, left + width, bottom + height + band])
    # Remaining boxes would cut the new band off; drop them so every viewer shows the full page.
    for box_name in ("/CropBox", "/TrimBox", "/BleedBox", "/ArtBox"):
        if box_name in page:
            del page[NameObject(box_name)]

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
    baseline = bottom + height + (band - font_size) / 2 + font_size * 0.2
    content = DecodedStreamObject()
    content.set_data(
        b"BT /FHeading %.2f Tf %.2f %.2f Td (" % (font_size, left + margin, baseline)
        + _escape_pdf_text(heading)
        + b") Tj ET"
    )
    heading_page[NameObject("/Contents")] = content
    page.merge_page(heading_page)


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
                added_page = writer.add_page(page)
                if page_index == 0 and heading and heading.strip():
                    _add_heading(added_page, heading.strip())
        except Exception as e:
            logger.error(f"Error appending document at index {index} to parent PDF: {e}", exc_info=True)
            raise

    merged = BytesIO()
    writer.write(merged)
    writer.close()
    return merged.getvalue()
