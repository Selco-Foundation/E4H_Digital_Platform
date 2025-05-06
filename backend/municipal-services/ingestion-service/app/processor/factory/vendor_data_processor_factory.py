from app.ingest.boundary_code_validator import BoundaryCodeValidator
from app.ingest.excel_data_loader import ExcelDataLoader
from app.ingest.excel_data_writer import ExcelDataWriter
from app.ingest.pattern_validator import PatternValidator
from app.ingest.required_field_validator import RequiredFieldValidator
from app.processor.vendor_data_processor import VendorDataProcessor
from app.schemas.request_info import RequestInfo
from app.utils.mdms_client import MDMSClient


class VendorDataProcessorFactory:
    @staticmethod
    def create_processor(
            file_path: str,
            vendor_sheet: str = "Vendor Input",
            boundary_sheet: str = "Boundary Code",
            output_sheet: str = "Vendor Output",
            mdms_url: str = None,
            request_info: RequestInfo = None
    ) -> VendorDataProcessor:
        data_loader = ExcelDataLoader(file_path, vendor_sheet, boundary_sheet)
        validators = []
        data_loader.load_data()

        def add_validation_columns(processor):
            data = processor.get_vendor_data().copy()
            if "status" not in data.columns:
                data["status"] = None
            else:
                data = data[data["status"] != "success"]
            if "error" not in data.columns:
                data["error"] = ""

            processor.set_vendor_data(data)
            return processor

        data_loader = add_validation_columns(data_loader)

        if mdms_url:
            mdms_client = MDMSClient(mdms_url)
            try:
                schema = mdms_client.fetch_vendor_schema(request_info)
                validators.append(RequiredFieldValidator(schema.mdms[0].data.columns))
                validators.append(PatternValidator(schema.mdms[0].data.columns))
            except Exception as e:
                print(f"Error: Could not set up MDMS validators - {e}")
                raise Exception("Could not set up MDMS validators")
        validators.append(BoundaryCodeValidator(data_loader.get_boundary_codes()))
        data_writer = ExcelDataWriter(file_path, output_sheet)

        return VendorDataProcessor(data_loader, validators, data_writer)