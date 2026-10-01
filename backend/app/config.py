import os
from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    ENVIRONMENT: str = "development"
    LOG_LEVEL: str = "INFO"
    DATABASE_URL: str = "sqlite:///./medsure.db"
    JWT_SECRET: str = "super_secret_jwt_key_powerbank_hackathon_2026_change_in_production"
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 1440
    
    # AI settings
    LLM_PROVIDER: str = "gemini"
    LLM_API_KEY: Optional[str] = None
    LLM_MODEL: str = "gemini-1.5-flash"
    
    # OCR settings
    OCR_PROVIDER: str = "pymupdf"
    OCR_CONFIDENCE_THRESHOLD: float = 0.70
    
    # FCM settings
    FIREBASE_CREDENTIALS_PATH: Optional[str] = None
    
    # Storage
    UPLOAD_DIR: str = "./uploads"
    
    # Demo credentials
    DEMO_PASSWORD: str = "MedSure2026!"
    
    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()
