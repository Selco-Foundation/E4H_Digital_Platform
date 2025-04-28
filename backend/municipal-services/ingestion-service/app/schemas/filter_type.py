from enum import Enum


class FilterType(Enum):
    ONE_OF = "ONE_OF"
    RANGE = "RANGE"
    REGEX = "REGEX"