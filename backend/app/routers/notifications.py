from typing import List, Dict, Any
from fastapi import APIRouter, Depends
from app.models.entities import User
from app.auth.rbac import get_current_user
from app.notifications import notification_service

router = APIRouter(prefix="/notifications", tags=["Notifications"])

@router.get("/poll", response_model=List[Dict[str, Any]])
def poll_notifications(current_user: User = Depends(get_current_user)):
    return notification_service.get_notifications_for_user(
        user_id=current_user.id,
        role=current_user.role
    )
