import logging

from pythonjsonlogger import jsonlogger

from app.core.singleton import SingletonMeta


# ---------------------------------------------------------------------------
# Custom TRACE log level
# ---------------------------------------------------------------------------
TRACE_LEVEL_NUM = 5
logging.addLevelName(TRACE_LEVEL_NUM, "TRACE")


def trace(self: logging.Logger, message: str, *args, **kws) -> None:
    """
    Add a lightweight TRACE level below DEBUG.

    This makes existing calls like `logger.trace(...)` work without
    breaking the standard logging API. By default, basicConfig is set
    to INFO, so TRACE messages will be suppressed unless the level is
    lowered explicitly.
    """
    if self.isEnabledFor(TRACE_LEVEL_NUM):
        self._log(TRACE_LEVEL_NUM, message, args, **kws)


if not hasattr(logging.Logger, "trace"):
    logging.Logger.trace = trace  # type: ignore[attr-defined]


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
