import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import engine, Base, SessionLocal
from app.services.seed_data import seed_database
from app.routers import (
    health, auth, cases, documents, discharge,
    bills, queries, insurance, checkin, alerts,
    approvals, notifications
)

# Initialize database schema
Base.metadata.create_all(bind=engine)

# Seed demo users & case
db = SessionLocal()
try:
    seed_database(db)
finally:
    db.close()

app = FastAPI(
    title="MedSure Backend API",
    description="After-Discharge Healthcare Coordination System for Patient, Family, and Hospital Desk (Team PowerBank)",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router)
app.include_router(auth.router)
app.include_router(cases.router)
app.include_router(documents.router)
app.include_router(discharge.router)
app.include_router(bills.router)
app.include_router(queries.router)
app.include_router(insurance.router)
app.include_router(checkin.router)
app.include_router(alerts.router)
app.include_router(approvals.router)
app.include_router(notifications.router)

@app.get("/")
def root():
    return {
        "message": "Welcome to MedSure API",
        "docs": "/docs",
        "team": "Team PowerBank",
        "demo_accounts": {
            "patient": "patient@medsure.demo",
            "family": "family@medsure.demo",
            "hospital": "hospital@medsure.demo",
            "password": "MedSure2026!"
        }
    }
