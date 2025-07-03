import pandas as pd


class DataWriter:
    def write_data(self, data: pd.DataFrame) -> bool:
        raise NotImplementedError("Subclasses must implement write_data")
