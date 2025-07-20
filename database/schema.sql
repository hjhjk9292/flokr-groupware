-- MySQL 데이터베이스 생성 및 사용자 설정
CREATE DATABASE IF NOT EXISTS flokr_groupware CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'flokr'@'localhost' IDENTIFIED BY 'flokr123!';
GRANT ALL PRIVILEGES ON flokr_groupware.* TO 'flokr'@'localhost';
USE flokr_groupware;

-- 기존 테이블 삭제 (참조 관계를 고려한 역순)
DROP TABLE IF EXISTS ATTACHMENT;
DROP TABLE IF EXISTS FACILITY_RESERVATION;
DROP TABLE IF EXISTS FACILITY;
DROP TABLE IF EXISTS SCHEDULE_ATTENDEE;
DROP TABLE IF EXISTS SCHEDULE;
DROP TABLE IF EXISTS NOTICE;
DROP TABLE IF EXISTS ADDRESS_ENTRY_GROUP_MAP;
DROP TABLE IF EXISTS ADDRESS_ENTRY;
DROP TABLE IF EXISTS ADDRESS_GROUP;
DROP TABLE IF EXISTS ATTENDANCE;
DROP TABLE IF EXISTS NOTIFICATION;
DROP TABLE IF EXISTS CHAT_ROOM_MEMBER;
DROP TABLE IF EXISTS CHAT_MESSAGE;
DROP TABLE IF EXISTS CHAT_ROOM;
DROP TABLE IF EXISTS APPROVAL_LINE;
DROP TABLE IF EXISTS APPROVAL_DOC;
DROP TABLE IF EXISTS APPROVAL_FORM;
DROP TABLE IF EXISTS TASK_COMMENT;
DROP TABLE IF EXISTS TASK_ASSIGNEE;
DROP TABLE IF EXISTS TASK;
DROP TABLE IF EXISTS EMPLOYEE;
DROP TABLE IF EXISTS POSITION;
DROP TABLE IF EXISTS DEPARTMENT;

-- ============================================================================
-- 테이블 생성 순서: 참조되는 테이블부터 생성 (부모 → 자식 순서)
-- 1. 독립 테이블 (참조하지 않는 테이블)
-- 2. 1차 참조 테이블 (독립 테이블만 참조)
-- 3. 2차 참조 테이블 (1차 참조 테이블 참조)
-- 4. 기타 복합 참조 테이블
-- ============================================================================

-- ============================================================================  
-- 1. 부서 테이블 (독립 테이블)
-- ============================================================================ 
CREATE TABLE DEPARTMENT ( 
    DEPT_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    DEPT_NAME VARCHAR(100) NOT NULL, 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')) 
); 

-- ============================================================================ 
-- 2. 직급 테이블 (독립 테이블)
-- ============================================================================ 
CREATE TABLE `POSITION` ( 
    POSITION_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    POSITION_NAME VARCHAR(100) NOT NULL, 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')) 
); 

-- ============================================================================ 
-- 3. 시설 테이블 (독립 테이블)
-- ============================================================================ 
CREATE TABLE FACILITY ( 
    FACILITY_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    FACILITY_NAME VARCHAR(100) NOT NULL, 
    FACILITY_LOCATION VARCHAR(255), 
    CAPACITY INT, 
    DESCRIPTION TEXT, 
    IMAGE_PATH VARCHAR(255) 
); 

-- ============================================================================ 
-- 4. 직원 테이블 (DEPARTMENT, POSITION 참조)
-- ============================================================================ 
CREATE TABLE EMPLOYEE ( 
    EMP_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    EMP_NAME VARCHAR(100) NOT NULL, 
    EMP_ID VARCHAR(50) UNIQUE NOT NULL, 
    PASSWORD_HASH VARCHAR(255) NOT NULL, 
    EMAIL VARCHAR(255) UNIQUE NOT NULL, 
    PHONE VARCHAR(20), 
    DEPT_NO BIGINT NOT NULL, 
    POSITION_NO BIGINT NOT NULL, 
    HIRE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    PROFILE_IMG_PATH VARCHAR(255), 
    SIGNATURE_IMG_PATH VARCHAR(255), 
    IS_ADMIN CHAR(1) DEFAULT 'N' NOT NULL CHECK (IS_ADMIN IN ('Y', 'N')), 
    LAST_LOGIN_DATE DATETIME, 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    UPDATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_dept_no (DEPT_NO),
    INDEX idx_position_no (POSITION_NO),
    FOREIGN KEY (DEPT_NO) REFERENCES DEPARTMENT(DEPT_NO),
    FOREIGN KEY (POSITION_NO) REFERENCES `POSITION`(POSITION_NO)
); 

-- ============================================================================ 
-- 5. 업무 테이블 (EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE TASK ( 
    TASK_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    TITLE VARCHAR(255) NOT NULL, 
    TASK_CONTENT TEXT, 
    CATEGORY VARCHAR(30) NOT NULL, 
    CREATOR_EMP_NO BIGINT, 
    DUE_DATE DATETIME, 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    UPDATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    TASK_STATUS VARCHAR(50) DEFAULT 'REQUEST' CHECK (TASK_STATUS IN ('REQUEST', 'IN_PROGRESS', 'FEEDBACK', 'HOLD', 'DONE')),
    EMOJI VARCHAR(10),
    INDEX idx_creator_emp_no (CREATOR_EMP_NO),
    FOREIGN KEY (CREATOR_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 6. 결재 양식 테이블 (EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE APPROVAL_FORM ( 
    FORM_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    FORM_NAME VARCHAR(100) NOT NULL, 
    DESCRIPTION TEXT, 
    IS_ACTIVE TINYINT(1) DEFAULT 1, 
    CREATE_EMP_NO BIGINT, 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_create_emp_no (CREATE_EMP_NO),
    FOREIGN KEY (CREATE_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 7. 채팅방 테이블 (EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE CHAT_ROOM (
    ROOM_NO BIGINT AUTO_INCREMENT PRIMARY KEY,
    ROOM_NAME VARCHAR(100),
    ROOM_TYPE VARCHAR(50) NOT NULL CHECK (ROOM_TYPE IN ('P', 'G')),
    CREATED_BY_EMP_NO BIGINT,
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP,
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_created_by_emp_no (CREATED_BY_EMP_NO),
    FOREIGN KEY (CREATED_BY_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
);

-- ============================================================================ 
-- 8. 공지사항 테이블 (EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE NOTICE ( 
    NOTICE_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    AUTHOR_EMP_NO BIGINT NOT NULL, 
    NOTICE_TITLE VARCHAR(255) NOT NULL, 
    NOTICE_CONTENT TEXT NOT NULL, 
    IS_MANDATORY TINYINT(1) DEFAULT 0 NOT NULL, 
    CATEGORY VARCHAR(100), 
    VIEW_COUNT INT DEFAULT 0 NOT NULL, 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    UPDATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_author_emp_no (AUTHOR_EMP_NO),
    FOREIGN KEY (AUTHOR_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 9. 일정 테이블 (EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE SCHEDULE ( 
    SCHEDULE_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    CREATE_EMP_NO BIGINT NOT NULL, 
    SCHEDULE_TITLE VARCHAR(255) NOT NULL, 
    DESCRIPTION TEXT, 
    START_DATE DATETIME, 
    END_DATE DATETIME, 
    LOCATION VARCHAR(255), 
    IMPORTANT VARCHAR(50) DEFAULT 'NORMAL' CHECK (IMPORTANT IN ('LOW', 'NORMAL', 'HIGH')), 
    SCHEDULE_TYPE VARCHAR(50) DEFAULT 'PERSONAL' NOT NULL CHECK (SCHEDULE_TYPE IN ('PERSONAL', 'TEAM', 'COMPANY', 'OTHER')),
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    UPDATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_create_emp_no (CREATE_EMP_NO),
    FOREIGN KEY (CREATE_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 10. 알림 테이블 (EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE NOTIFICATION ( 
    NOTIFICATION_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    RECIPIENT_EMP_NO BIGINT NOT NULL, 
    TYPE VARCHAR(50) NOT NULL CHECK (TYPE IN ('APPROVAL', 'TASK', 'CHAT', 'NOTICE', 'FACILITY')), 
    TITLE VARCHAR(255) NOT NULL, 
    NOTIFICATION_CONTENT TEXT, 
    REF_TYPE VARCHAR(50), 
    REF_NO VARCHAR(255), 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    read_DATE DATETIME,
    INDEX idx_recipient_emp_no (RECIPIENT_EMP_NO),
    FOREIGN KEY (RECIPIENT_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 11. 근태 기록 테이블 (EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE ATTENDANCE ( 
    ATTENDANCE_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    EMP_NO BIGINT NOT NULL, 
    ATTENDANCE_DATE DATE NOT NULL, 
    CLOCK_IN_TIME DATETIME, 
    CLOCK_OUT_TIME DATETIME, 
    ATT_STATUS VARCHAR(50) CHECK (ATT_STATUS IN ('NORMAL', 'LATE', 'ABSENCE', 'VACATION', 'REMOTE')), 
    UNIQUE KEY UK_EMP_ATTENDANCE_DATE (EMP_NO, ATTENDANCE_DATE),
    INDEX idx_emp_no (EMP_NO),
    FOREIGN KEY (EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 12. 주소록 그룹 테이블 (EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE ADDRESS_GROUP ( 
    GROUP_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    OWNER_EMP_NO BIGINT NOT NULL, 
    GROUP_NAME VARCHAR(100) NOT NULL, 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')), 
    UNIQUE KEY UK_ADDRESS_GROUP (OWNER_EMP_NO, GROUP_NAME),
    INDEX idx_owner_emp_no (OWNER_EMP_NO),
    FOREIGN KEY (OWNER_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 13. 업무 담당자 테이블 (TASK, EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE TASK_ASSIGNEE ( 
    TASK_ASSIGNEE_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    TASK_NO BIGINT NOT NULL, 
    ASSIGNEE_EMP_NO BIGINT NOT NULL, 
    ASSIGN_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    UNIQUE KEY UK_TASK_ASSIGNEE (TASK_NO, ASSIGNEE_EMP_NO),
    INDEX idx_task_no (TASK_NO),
    INDEX idx_assignee_emp_no (ASSIGNEE_EMP_NO),
    FOREIGN KEY (TASK_NO) REFERENCES TASK(TASK_NO),
    FOREIGN KEY (ASSIGNEE_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 14. 업무 댓글 테이블 (TASK, EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE TASK_COMMENT ( 
    COMMENT_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    TASK_NO BIGINT NOT NULL, 
    AUTHOR_EMP_NO BIGINT NOT NULL, 
    TASK_CMT_CONTENT TEXT NOT NULL, 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    UPDATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_task_no (TASK_NO),
    INDEX idx_author_emp_no (AUTHOR_EMP_NO),
    FOREIGN KEY (TASK_NO) REFERENCES TASK(TASK_NO),
    FOREIGN KEY (AUTHOR_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 15. 결재 문서 테이블 (APPROVAL_FORM, EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE APPROVAL_DOC ( 
    DOC_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    FORM_NO BIGINT NOT NULL, 
    DRAFTER_EMP_NO BIGINT NOT NULL, 
    TITLE VARCHAR(255) NOT NULL, 
    DOC_CONTENT TEXT NOT NULL, 
    REQUESTED_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    COMPLETED_DATE DATETIME, 
    VERSION INT DEFAULT 1, 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    UPDATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    DOC_STATUS VARCHAR(50) DEFAULT 'DRAFT' CHECK (DOC_STATUS IN ('DRAFT', 'REQUESTED', 'APPROVED', 'REJECTED', 'CANCELED')), 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_form_no (FORM_NO),
    INDEX idx_drafter_emp_no (DRAFTER_EMP_NO),
    FOREIGN KEY (FORM_NO) REFERENCES APPROVAL_FORM(FORM_NO),
    FOREIGN KEY (DRAFTER_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 16. 채팅 메시지 테이블 (CHAT_ROOM, EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE CHAT_MESSAGE ( 
    MESSAGE_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    ROOM_NO BIGINT NOT NULL, 
    SENDER_EMP_NO BIGINT NOT NULL, 
    CHAT_CONTENT TEXT NOT NULL, 
    MESSAGE_TYPE VARCHAR(50) DEFAULT 'TEXT' CHECK (MESSAGE_TYPE IN ('TEXT', 'FILE', 'IMAGE', 'CODE')), 
    SEND_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_room_no (ROOM_NO),
    INDEX idx_sender_emp_no (SENDER_EMP_NO),
    FOREIGN KEY (ROOM_NO) REFERENCES CHAT_ROOM(ROOM_NO),
    FOREIGN KEY (SENDER_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 17. 일정 참석자 테이블 (SCHEDULE, EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE SCHEDULE_ATTENDEE ( 
    ATTENDEE_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    SCHEDULE_NO BIGINT NOT NULL, 
    EMP_NO BIGINT NOT NULL, 
    RESPONSE_STATUS VARCHAR(20) DEFAULT 'PENDING' CHECK (RESPONSE_STATUS IN ('PENDING', 'ACCEPTED', 'DECLINED')), 
    NOTIFICATION_SENT TINYINT(1) DEFAULT 0, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),	 
    UNIQUE KEY UK_SCHEDULE_ATTENDEE (SCHEDULE_NO, EMP_NO),
    INDEX idx_schedule_no (SCHEDULE_NO),
    INDEX idx_emp_no (EMP_NO),
    FOREIGN KEY (SCHEDULE_NO) REFERENCES SCHEDULE(SCHEDULE_NO),
    FOREIGN KEY (EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 18. 시설 예약 테이블 (FACILITY, EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE FACILITY_RESERVATION ( 
    RESERVATION_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    FACILITY_NO BIGINT NOT NULL, 
    RESERVER_EMP_NO BIGINT NOT NULL, 
    START_TIME DATETIME NOT NULL, 
    END_TIME DATETIME NOT NULL, 
    PURPOSE VARCHAR(255), 
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    UPDATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    RES_STATUS VARCHAR(10) DEFAULT 'PENDING' CHECK (RES_STATUS IN ('APPROVED', 'PENDING', 'CANCELED')),
    INDEX idx_facility_no (FACILITY_NO),
    INDEX idx_reserver_emp_no (RESERVER_EMP_NO),
    FOREIGN KEY (FACILITY_NO) REFERENCES FACILITY(FACILITY_NO),
    FOREIGN KEY (RESERVER_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 19. 주소록 항목 테이블 (EMPLOYEE 참조, 순환 참조 주의)
-- ============================================================================ 
CREATE TABLE ADDRESS_ENTRY ( 
    ENTRY_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    ENTRY_TYPE VARCHAR(50) NOT NULL CHECK (ENTRY_TYPE IN ('PERSONAL', 'COMPANY')), 
    OWNER_EMP_NO BIGINT NOT NULL, 
    ADD_NAME VARCHAR(100) NOT NULL, 
    ADD_PHONE VARCHAR(20), 
    ADD_EMAIL VARCHAR(255), 
    COMPANY VARCHAR(100), 
    POSITION VARCHAR(100), 
    MEMO TEXT, 
    RELATED_EMP_NO BIGINT,  
    CREATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    UPDATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_owner_emp_no (OWNER_EMP_NO),
    INDEX idx_related_emp_no (RELATED_EMP_NO),
    FOREIGN KEY (OWNER_EMP_NO) REFERENCES EMPLOYEE(EMP_NO),
    FOREIGN KEY (RELATED_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 20. 결재선 테이블 (APPROVAL_DOC, EMPLOYEE 참조)
-- ============================================================================ 
CREATE TABLE APPROVAL_LINE ( 
    LINE_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    DOC_NO BIGINT NOT NULL, 
    APPROVER_EMP_NO BIGINT NOT NULL, 
    APPROVAL_ORDER INT NOT NULL, 
    APPROVAL_COMMENT TEXT, 
    PROCESSED_DATE DATETIME, 
    LINE_STATUS VARCHAR(50) DEFAULT 'PENDING' CHECK (LINE_STATUS IN ('WAITING', 'APPROVED', 'REJECTED', 'PENDING')),
    INDEX idx_doc_no (DOC_NO),
    INDEX idx_approver_emp_no (APPROVER_EMP_NO),
    FOREIGN KEY (DOC_NO) REFERENCES APPROVAL_DOC(DOC_NO),
    FOREIGN KEY (APPROVER_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================ 
-- 21. 채팅방 멤버 테이블 (CHAT_ROOM, EMPLOYEE, CHAT_MESSAGE 참조)
-- ============================================================================ 
CREATE TABLE CHAT_ROOM_MEMBER ( 
    MEMBER_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    ROOM_NO BIGINT NOT NULL, 
    EMP_NO BIGINT NOT NULL, 
    JOIN_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    LAST_READ_MESSAGE_NO BIGINT, 
    IS_ADMIN CHAR(1) DEFAULT 'N' CHECK (IS_ADMIN IN ('Y', 'N')), 
    NOTIFICATION_ENABLED TINYINT(1) DEFAULT 1, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')), 
    UNIQUE KEY UK_CHAT_ROOM_MEMBER (ROOM_NO, EMP_NO),
    INDEX idx_room_no (ROOM_NO),
    INDEX idx_emp_no (EMP_NO),
    INDEX idx_last_read_message_no (LAST_READ_MESSAGE_NO),
    FOREIGN KEY (ROOM_NO) REFERENCES CHAT_ROOM(ROOM_NO),
    FOREIGN KEY (EMP_NO) REFERENCES EMPLOYEE(EMP_NO),
    FOREIGN KEY (LAST_READ_MESSAGE_NO) REFERENCES CHAT_MESSAGE(MESSAGE_NO) ON DELETE SET NULL
); 

-- ============================================================================ 
-- 22. 주소록 항목-그룹 매핑 테이블 (ADDRESS_ENTRY, ADDRESS_GROUP 참조)
-- ============================================================================ 
CREATE TABLE ADDRESS_ENTRY_GROUP_MAP ( 
    MAP_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    ENTRY_NO BIGINT NOT NULL, 
    GROUP_NO BIGINT NOT NULL, 
    UNIQUE KEY UK_ENTRY_GROUP (ENTRY_NO, GROUP_NO),
    INDEX idx_entry_no (ENTRY_NO),
    INDEX idx_group_no (GROUP_NO),
    FOREIGN KEY (ENTRY_NO) REFERENCES ADDRESS_ENTRY(ENTRY_NO),
    FOREIGN KEY (GROUP_NO) REFERENCES ADDRESS_GROUP(GROUP_NO)
); 

-- ============================================================================ 
-- 23. 첨부파일 테이블 (EMPLOYEE 참조, 다른 테이블 동적 참조)
-- ============================================================================ 
CREATE TABLE ATTACHMENT ( 
    ATTACHMENT_NO BIGINT AUTO_INCREMENT PRIMARY KEY, 
    REF_TYPE VARCHAR(50) NOT NULL, 
    REF_NO BIGINT NOT NULL, 
    UPLOADER_EMP_NO BIGINT NOT NULL, 
    ORIGINAL_FILENAME VARCHAR(255) NOT NULL, 
    STORED_FILEPATH VARCHAR(500) NOT NULL, 
    FILE_EXTENSION VARCHAR(20), 
    UPLOAD_DATE DATETIME DEFAULT CURRENT_TIMESTAMP, 
    STATUS CHAR(1) DEFAULT 'Y' CHECK (STATUS IN ('Y', 'N')),
    INDEX idx_uploader_emp_no (UPLOADER_EMP_NO),
    FOREIGN KEY (UPLOADER_EMP_NO) REFERENCES EMPLOYEE(EMP_NO)
); 

-- ============================================================================
-- 기본 데이터 삽입 (필수 데이터만)
-- ============================================================================

-- 부서 데이터
INSERT INTO DEPARTMENT (DEPT_NO, DEPT_NAME, CREATE_DATE, STATUS) VALUES 
(1, '인사팀', '2024-01-05', 'Y'),
(2, '재무팀', '2024-01-10', 'Y'),
(3, '마케팅팀', '2024-01-15', 'Y'),
(4, '개발팀', '2024-01-20', 'Y'),
(5, '영업팀', '2024-01-25', 'Y');

-- 직급 데이터
INSERT INTO POSITION (POSITION_NO, POSITION_NAME, CREATE_DATE, STATUS) VALUES 
(1, '사원', '2024-01-05', 'Y'),
(2, '대리', '2024-01-05', 'Y'),
(3, '과장', '2024-01-05', 'Y'),
(4, '차장', '2024-01-05', 'Y'),
(5, '부장', '2024-01-05', 'Y');

-- 시설 데이터
INSERT INTO FACILITY (FACILITY_NO, FACILITY_NAME, FACILITY_LOCATION, CAPACITY, DESCRIPTION, IMAGE_PATH) VALUES
(1, '대회의실', '본관 3층', 30, '대규모 회의 및 발표에 적합한 대형 회의실', '/images/facility/meeting_large.png'),
(2, '소회의실', '본관 2층', 10, '소규모 미팅용 회의실', '/images/facility/meeting_small.png');

-- 직원 데이터 (관리자와 테스트 직원)
INSERT INTO EMPLOYEE (EMP_NO, EMP_NAME, EMP_ID, PASSWORD_HASH, EMAIL, PHONE, DEPT_NO, POSITION_NO, HIRE_DATE, IS_ADMIN, CREATE_DATE, STATUS) VALUES
(1, '관리자', 'admin', '$2a$10$N.zmdr9k7uOCQb96VvVfoO6NC0.8vKcKPhBWkH6RFVN1MZB4z5VsG', 'admin@flokr.com', '010-1234-5678', 1, 3, '2020-03-10', 'Y', '2025-01-02', 'Y'),
(2, '이영희', 'lee002', '$2a$10$N.zmdr9k7uOCQb96VvVfoO6NC0.8vKcKPhBWkH6RFVN1MZB4z5VsG', 'lee@flokr.com', '010-2345-6789', 2, 2, '2021-05-15', 'N', '2025-01-02', 'Y');

-- 성능 최적화 인덱스
CREATE INDEX idx_employee_emp_id ON EMPLOYEE(EMP_ID);
CREATE INDEX idx_employee_email ON EMPLOYEE(EMAIL);
CREATE INDEX idx_notification_recipient_date ON NOTIFICATION(RECIPIENT_EMP_NO, CREATE_DATE);
CREATE INDEX idx_chat_message_room_date ON CHAT_MESSAGE(ROOM_NO, SEND_DATE);
CREATE INDEX idx_attendance_emp_date ON ATTENDANCE(EMP_NO, ATTENDANCE_DATE);
CREATE INDEX idx_task_status_date ON TASK(TASK_STATUS, CREATE_DATE);
CREATE INDEX idx_approval_doc_status_date ON APPROVAL_DOC(DOC_STATUS, CREATE_DATE);

COMMIT;
