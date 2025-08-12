import logging

from pythonjsonlogger import jsonlogger

from app.core.singleton import SingletonMeta


class AppLogger(metaclass=SingletonMeta):
    _logger = None

    def __init__(self):
        logging.basicConfig(level=logging.INFO)
        self._logger = logging.getLogger(__name__)
        log_handler = logging.StreamHandler()
        formatter = jsonlogger.JsonFormatter(
            "%(asctime)s %(name)s %(levelname)s %(message)s"
        )
        log_handler.setFormatter(formatter)
        self._logger.addHandler(log_handler)

    def get_logger(self) -> logging.Logger:
        assert self._logger is not None
        return self._logger
