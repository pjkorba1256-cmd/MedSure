from datetime import datetime
from typing import Dict, Any, List, Optional
from collections import defaultdict
import logging

logger = logging.getLogger(__name__)

class NotificationMessage:
    def __init__(self, target_user_id: str, title: str, body: str, event_type: str, data: Dict[str, Any] = None):
        self.id = f"notif_{int(datetime.utcnow().timestamp()*1000)}"
        self.target_user_id = target_user_id
        self.title = title
        self.body = body
        self.event_type = event_type
        self.data = data or {}
        self.timestamp = datetime.utcnow().isoformat()
        self.read = False

class NotificationService:
    def __init__(self):
        self._user_queues: Dict[str, List[NotificationMessage]] = defaultdict(list)
        self._fcm_enabled = False

    def send_notification(self, target_user_id: str, title: str, body: str, event_type: str, data: Dict[str, Any] = None) -> NotificationMessage:
        msg = NotificationMessage(
            target_user_id=target_user_id,
            title=title,
            body=body,
            event_type=event_type,
            data=data
        )
        self._user_queues[target_user_id].append(msg)
        logger.info(f"[NOTIF] Queued notification to user {target_user_id}: {title} ({event_type})")
        return msg

    def broadcast_to_role(self, role: str, title: str, body: str, event_type: str, data: Dict[str, Any] = None):
        msg = NotificationMessage(
            target_user_id=role,
            title=title,
            body=body,
            event_type=event_type,
            data=data
        )
        self._user_queues[role].append(msg)
        logger.info(f"[NOTIF BROADCAST] To {role}: {title}")

    def get_notifications_for_user(self, user_id: str, role: Optional[str] = None) -> List[Dict[str, Any]]:
        messages = list(self._user_queues.get(user_id, []))
        if role:
            messages.extend(self._user_queues.get(role, []))
        
        messages.sort(key=lambda m: m.timestamp, reverse=True)
        return [
            {
                "id": m.id,
                "target_user_id": m.target_user_id,
                "title": m.title,
                "body": m.body,
                "event_type": m.event_type,
                "data": m.data,
                "timestamp": m.timestamp,
                "read": m.read
            }
            for m in messages
        ]

    def clear_notifications(self, user_id: str):
        if user_id in self._user_queues:
            self._user_queues[user_id] = []

notification_service = NotificationService()
