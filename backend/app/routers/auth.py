from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import User
from app.schemas import Token, LoginRequest, UserResponse
from app.auth.password import verify_password
from app.auth.jwt_handler import create_access_token
from app.auth.rbac import get_current_user

router = APIRouter(prefix='/auth', tags=['Authentication'])

@router.post('/login', response_model=Token)
def login(creds: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == creds.email).first()
    if not user or not verify_password(creds.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail='Invalid email or password',
            headers={'WWW-Authenticate': 'Bearer'}
        )
    
    token = create_access_token(data={'user_id': user.id, 'role': user.role})
    return {
        'access_token': token,
        'token_type': 'bearer',
        'user': user
    }

@router.get('/me', response_model=UserResponse)
def get_me(current_user: User = Depends(get_current_user)):
    return current_user
