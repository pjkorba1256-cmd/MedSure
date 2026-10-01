import pytest
from app.ocr.ocr_service import ocr_service

def test_pdf_ocr_extraction():
    pdf_path = "d:/MedSure/mock_data/discharge/discharge_summary_ms001.pdf"
    result = ocr_service.extract_document(pdf_path)
    
    assert result.page_count == 2
    assert "Rajesh Sharma" in result.text
    assert "Metformin" in result.text
    assert result.overall_confidence > 0.70
    assert result.is_low_confidence is False

def test_image_ocr_low_confidence_flag():
    img_path = "d:/MedSure/mock_data/discharge/scanned_discharge_page.png"
    result = ocr_service.extract_document(img_path)
    
    assert result.page_count == 1
    # Simulated scanned image confidence is 0.65 (< 0.70 threshold)
    assert result.overall_confidence < 0.70
    assert result.is_low_confidence is True
