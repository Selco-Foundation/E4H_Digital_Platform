from app.ingest.boundary_code_validator import BoundaryCodeValidator
from app.ingest.excel_data_loader import ExcelDataLoader
from app.ingest.excel_data_writer import ExcelDataWriter
from app.ingest.mdms_validator import MDMSValidator
from app.ingest.pattern_validator import PatternValidator
from app.ingest.required_field_validator import RequiredFieldValidator
from app.processor.vendor_data_processor import VendorDataProcessor
from app.utils.mdms_client import MDMSClient


class VendorDataProcessorFactory:
    @staticmethod
    def create_processor(
            file_path: str,
            vendor_sheet: str = "Vendor Input",
            boundary_sheet: str = "Boundary Code",
            output_sheet: str = "Vendor Output",
            mdms_url: str = None,
            org_service_url: str = None
    ) -> VendorDataProcessor:
        data_loader = ExcelDataLoader(file_path, vendor_sheet, boundary_sheet)
        validators = []

        if mdms_url:
            mdms_client = MDMSClient(mdms_url)
            try:
                schema = mdms_client.fetch_vendor_schema()
                validators.append(RequiredFieldValidator(schema.columns))
                validators.append(PatternValidator(schema.columns))
                validators.append(MDMSValidator(schema.columns, mdms_client))
            except Exception as e:
                print(f"Error: Could not set up MDMS validators - {e}")
                raise Exception("Could not set up MDMS validators")

        data_loader.load_data()
        validators.append(BoundaryCodeValidator(data_loader.get_boundary_codes()))

        data_writer = ExcelDataWriter(file_path, output_sheet)

        return VendorDataProcessor(data_loader, validators, data_writer)
