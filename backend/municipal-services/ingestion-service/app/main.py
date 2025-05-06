# CodeReview: Add application metadata (title, description, version) for better API documentation
# CodeReview: Consider adding custom middleware for request logging and error tracking
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.routes import api_router

app = FastAPI(
    title="E4H Ingestion Service",
    description="Service for handling data ingestion through Excel files",
    version="1.0.0"
)

# CodeReview: Security concern - CORS is too permissive
# Consider restricting origins, methods, and headers based on environment
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # TODO: Replace with specific origins from config
    allow_credentials=True,
    allow_methods=["*"],  # TODO: Restrict to needed methods (GET, POST)
    allow_headers=["*"],  # TODO: Specify required headers only
)
app.include_router(api_router)


# CodeReview: Load configuration from environment variables
# CodeReview: Add proper logging configuration
if __name__ == "__main__":
    import uvicorn
    import os
    from dotenv import load_dotenv

    load_dotenv()
    port = int(os.getenv("PORT", 8000))
    host = os.getenv("HOST", "0.0.0.0")
    
    uvicorn.run(
        app,
        host=host,
        port=port,
        log_config="log_config.json"  # TODO: Add proper logging configuration
    )