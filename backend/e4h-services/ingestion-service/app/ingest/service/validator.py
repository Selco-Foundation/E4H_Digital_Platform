import pandas as pd


class Validator:
    def validate(self, data: pd.DataFrame) -> pd.DataFrame:
        raise NotImplementedError("Subclasses must implement validate")
