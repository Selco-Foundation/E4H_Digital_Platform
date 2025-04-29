from app.ingest.boundary_hierarchy_validator import BoundaryHierarchyValidator
from app.ingest.boundary_excel_data_loader import BoundaryExcelDataLoader
from app.ingest.excel_data_writer import ExcelDataWriter
from app.ingest.pattern_validator import PatternValidator
from app.ingest.required_field_validator import RequiredFieldValidator
from app.processor.boundary_data_processor import BoundaryDataProcessor
from app.schemas.request_info import RequestInfo
from app.utils.mdms_client import MDMSClient


class BoundaryDataProcessorFactory:
    @staticmethod
    def create_processor(
            file_path: str,
            boundary_sheet: str = "Boundary Data",
            output_sheet: str = "Boundary Output",
            mdms_url: str = None,
            request_info: RequestInfo = None
    ) -> BoundaryDataProcessor:
        # Initialize data loader with boundary sheet only
        data_loader = BoundaryExcelDataLoader(file_path, boundary_sheet=boundary_sheet)
        validators = []

        # Load the boundary data
        data_loader.load_data()

        # Set up MDMS-based validators if MDMS URL is provided
        if mdms_url:
            mdms_client = MDMSClient(mdms_url)
            try:
                schema = mdms_client.fetch_boundary_schema(request_info)
                validators.append(RequiredFieldValidator(schema.mdms[0].data.columns))
                validators.append(PatternValidator(schema.mdms[0].data.columns))
            except Exception as e:
                print(f"Error: Could not set up MDMS validators - {e}")
                raise Exception("Could not set up MDMS validators")

        # Add boundary-specific validators
        validators.append(BoundaryHierarchyValidator())

        # Initialize data writer for output
        data_writer = ExcelDataWriter(file_path, output_sheet)

        return BoundaryDataProcessor(data_loader, validators, data_writer, request_info)