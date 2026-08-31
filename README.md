# 시민 버스 민원센터 - JPA 실제 SMS 인증

Spring Boot 3.5.16, Java 17, Spring Data JPA, MySQL, Thymeleaf와
SOLAPI Java SDK 1.0.3으로 구성한 회원가입 프로젝트입니다.

## 구현 기능

- JPA 회원정보 저장
- 아이디 DB 중복확인
- BCrypt 비밀번호 암호화
- 실제 휴대전화 인증번호 SMS 발송
- 인증번호 원문 대신 BCrypt 해시 저장
- 인증번호 3분 만료
- 동일 번호 재발송 60초 제한
- 인증번호 최대 5회 실패 제한
- 인증 성공 후 10분 동안 유효한 일회용 가입 토큰 발급
- 회원가입 완료 시 인증 토큰 소비
- 휴대전화 번호를 변경하면 인증 결과 즉시 초기화
- 회원가입 계정으로 Spring Security 로그인
- `emp_status=Y` 회원만 로그인 허용
- `emp_role=U/A`를 `ROLE_USER/ROLE_ADMIN` 권한으로 변환
- 일반회원 로그인 성공 시 메인 화면 이동
- 관리자 권한(`emp_role=A`) 로그인 성공 시 관리자 대시보드 이동
- 로그인 성공 시 DB의 회원 이름과 마이페이지·로그아웃 메뉴 표시
- 로그인 실패 시 로그인 페이지 유지 및 불일치 팝업 표시
- 로그인 상태 유지 및 로그아웃
- 비로그인 상태에서 민원 접수 링크 클릭 시 페이지 중앙 로그인 안내 모달
- 로그인 회원 전용 민원 접수 페이지
- 민원 분류·제목·내용·첨부파일 프런트 입력 화면
- 로그인 회원정보를 DB에서 다시 조회하여 민원과 함께 저장
- 민원 및 첨부파일 메타데이터 MySQL 저장
- 첨부파일을 `uploads/complaints/{민원번호}`에 안전한 파일명으로 저장
- 한글 원본 파일명 UTF-8 보존 및 Unicode NFC 정규화
- 첨부파일 개별 취소와 전체 취소
- 접수 성공 중앙 팝업과 확인 후 메인 이동
- 민원 조회 화면 디자인(기간 설정·처리상태 탭·상세 아코디언)
- 전체·확인중·처리중·답변완료 프런트 상태 필터
- `complaint_status` 처리상태 컬럼과 `ComplaintStatus` Enum
- 로그인 회원의 `emp_no`와 기간을 조건으로 한 실제 DB 민원 조회
- 관리자 대시보드 민원 상태별 집계 및 최근 민원 조회
- 관리자 민원 전체·상태별 조회, 답변 등록·수정 및 처리상태 변경
- 대시보드 상태 카드 선택과 상태별 10건 게시판 페이징
- 대시보드·민원관리 제목 클릭 상세 팝업 및 민원 첨부파일 다운로드
- 관리자 답변 첨부파일 등록 및 다운로드(한글 파일명 지원)
- 고객 민원 상세에서 관리자 답변 첨부파일 확인 및 다운로드
- 로그인 회원 본인의 민원 첨부파일만 다운로드하도록 소유권 검증
- 공지사항 글꼴·글자 크기·굵게·기울임·밑줄·목록 편집 및 DB 등록
- 공지사항 본문 이미지 최대 5장 등록·삭제와 고객 상세 화면 표시
- 공지사항 본문 이미지 파일 선택 및 드래그 앤 드롭 삽입
- 공지별 메인 팝업 노출 설정과 브라우저별 24시간 숨김
- 관리자 공지사항 목록·등록 화면 분리와 최근순 10건 페이징
- 메인 화면 하단 서경운수 회사정보·연락처 푸터와 맨 위 이동 버튼
- 메인 민원 바로가기 순서·문구 변경 및 민원 처리 절차 3단계 표시
- 회원 전체·이용중·탈퇴·관리자 실시간 통계
- 회원 상태·권한·아이디·이름 조건 검색
- 관리자 회원 권한 변경(U 사용자/A 관리자) 및 자기 권한 강등 방지
- 회원 아이디 클릭 상세 팝업(이메일·주소·가입일시·수정일시 포함)
- 사용자 메인에서 관리자 권한 재확인 후 관리자페이지 복귀
- 관리자 메인페이지 관리에서 상단 배너 이미지 최대 5장 등록·삭제
- 등록 배너 5초 자동 전환, 마우스 오버 일시정지, 배너 수만큼 페이지 점 표시, 다음 화살표 이동
- 메인 상단 4개 그룹 메뉴와 데스크톱 드롭다운·모바일 아코디언
- 회사소개·노선운행안내·채용안내·고객센터 하위 메뉴의 공개 준비 중 페이지 연결
- 민원 분류를 칭찬합니다·불편합니다·분실물 문의로 구성
- 글꼴·글자 크기·굵게·기울임·밑줄·목록 서식이 가능한 민원 본문 편집기
- 게시글 비밀번호 BCrypt 암호화 저장과 5회 실패 시 1분 입력 제한
- 전체 공개 민원 게시판의 분류·제목 검색과 10건 단위 페이징
- 공개 목록 작성자 이름 마스킹 및 비밀번호 확인 후 상세·첨부파일 열람
- 고객센터 하위 메뉴와 메인 바로가기에서 해당 분류 공개 게시판으로 바로 이동
- 관리자 공지사항 분류·상단 고정·제목·내용 DB 등록
- 고객 공개 공지사항 제목 검색·10건 페이징·상세 조회 및 메인 최근 3건 노출

## 먼저 해야 할 일

### 1. MySQL 테이블 생성

MySQL Workbench에서 `database/schema.sql`을 실행합니다.

다음 테이블이 생성됩니다.

- `sgtransit_employee`
- `sgtransit_phone_verification`

`spring.jpa.hibernate.ddl-auto=validate`이므로 SQL을 먼저 실행하지 않으면
애플리케이션 시작 시 테이블 검증 오류가 발생합니다.

기존 회원가입 테이블이 이미 있다면 새 민원 기능 적용 전 다음 파일만 실행해도 됩니다.

```text
database/complaint-schema.sql
```

다음 테이블이 추가됩니다.

- `sgtransit_complaint`
- `sgtransit_complaint_attachment`

기존 민원 테이블에 처리상태 컬럼을 추가하거나 예전 `RECEIVED` 값을 변경할 때는
다음 파일을 실행합니다.

```text
database/complaint-status-migration.sql
```

기존 민원 테이블을 공개 비밀번호 게시판으로 변경할 때는 다음 파일도 실행합니다.

```text
database/complaint-public-board-migration.sql
```

이 스크립트는 `complaint_password` 컬럼과 분류·등록일 복합 인덱스를 추가하고,
이전 `DRIVER/BUS/GENERAL` 분류를 새 분류로 변환합니다. 이전 게시글은 접수 당시
게시글 비밀번호를 받지 않았으므로 비밀번호 재설정 전까지 상세 열람이 제한됩니다.

기존 게시글의 비어 있는 비밀번호를 `1234`의 BCrypt 해시로 일괄 설정하려면
다음 파일을 실행합니다.

```text
database/existing-complaint-password-1234.sql
```

기존 프로젝트에 공지사항 기능을 추가할 때는 다음 파일을 실행합니다.

```text
database/notice-schema.sql
```

이 스크립트는 공지사항 본문과 이미지 메타데이터를 저장하는
`sgtransit_notice`, `sgtransit_notice_image` 테이블을 생성합니다. 실제 이미지는
기본적으로 `uploads/notices/{공지사항번호}` 폴더에 저장됩니다. 저장 위치는 Eclipse
실행 환경변수 `NOTICE_IMAGE_UPLOAD_DIR`로 변경할 수 있습니다.

이미 `sgtransit_notice` 테이블이 생성된 프로젝트에 메인 공지 팝업 기능만 추가할
때는 다음 파일을 한 번 실행합니다. 이미 컬럼과 인덱스가 있으면 자동으로 건너뜁니다.

```text
database/notice-popup-migration.sql
```

민원 상태값은 다음 세 가지입니다.

```text
CHECKING   = 확인중 (신규 민원의 기본 상태)
PROCESSING = 처리중
COMPLETED  = 답변완료
```

기존 민원 테이블에 회원번호·등록일 복합 조회 인덱스를 추가하려면 다음 파일을 실행합니다.

```text
database/complaint-list-index.sql
```

관리자 답변 기능을 추가하는 기존 프로젝트에서는 다음 파일도 실행합니다.

```text
database/admin-answer-schema.sql
```

이 스크립트는 민원 한 건당 관리자 답변 한 건을 저장하는
`sgtransit_complaint_answer` 테이블과 답변 첨부파일을 저장하는
`sgtransit_complaint_answer_attachment` 테이블을 생성합니다.

관리자 답변 테이블은 이미 생성했고 첨부파일 테이블만 추가해야 한다면 다음 파일만 실행합니다.

```text
database/admin-answer-attachment-schema.sql
```

메인페이지 배너 관리 기능을 추가하는 기존 프로젝트에서는 다음 파일도 실행합니다.

```text
database/main-banner-schema.sql
```

이 스크립트는 메인 배너 이미지 메타데이터를 저장하는
`sgtransit_main_banner` 테이블을 생성합니다. 실제 이미지는 기본적으로
`uploads/main-banners` 폴더에 저장됩니다.

현재 일반회원 계정을 관리자 테스트 계정으로 변경하려면 MySQL에서 다음처럼 실행한 뒤
로그아웃하고 다시 로그인합니다.

```sql
UPDATE sgtransit_employee
SET emp_role = 'A'
WHERE emp_id = '관리자로_사용할_아이디';
```

권한은 로그인할 때 세션에 저장되므로 DB 값을 변경한 뒤에는 반드시 다시 로그인해야 합니다.

### 2. SOLAPI 계정 준비

1. SOLAPI 계정을 만듭니다.
2. 문자 발송에 사용할 발신번호를 등록합니다.
3. API Key와 API Secret을 발급합니다.
4. 실제 발송에 필요한 잔액을 충전합니다.

발신번호와 수신번호는 하이픈 없이 전달됩니다.

## Eclipse에 환경변수 등록

API 키를 `application.properties`에 직접 적지 않는 것을 권장합니다.

```text
Run
→ Run Configurations
→ Spring Boot App
→ 현재 애플리케이션 선택
→ Environment
→ New
```

다음 값을 등록합니다.

```text
DB_NAME=bus_complaint_db
DB_USERNAME=root
DB_PASSWORD=본인의 MySQL 비밀번호
SOLAPI_API_KEY=발급받은 API Key
SOLAPI_API_SECRET=발급받은 API Secret
SOLAPI_SENDER=등록한 발신번호(하이픈 제외)
```

설정 후 `Apply → Run`을 선택합니다.

## API 흐름

### 인증번호 요청

```text
signup.js
→ POST /api/phone-verifications/request
→ PhoneVerificationController
→ PhoneVerificationService
→ 인증번호 생성 및 BCrypt 해시 DB 저장
→ SolapiSmsSender
→ SOLAPI
→ 실제 휴대전화 SMS 수신
```

요청 JSON:

```json
{
  "phone": "01012345678"
}
```

### 인증번호 확인

```text
signup.js
→ POST /api/phone-verifications/verify
→ DB에 저장된 BCrypt 해시와 입력번호 비교
→ 성공 시 일회용 verificationToken 반환
→ hidden input에 보관
```

요청 JSON:

```json
{
  "phone": "01012345678",
  "code": "123456"
}
```

### 회원가입

```text
POST /signup
→ EmployeeService
→ 휴대전화 번호와 verificationToken 재검증
→ 토큰 사용 처리
→ EmployeeRepository.saveAndFlush()
→ sgtransit_employee 저장
```

JavaScript의 hidden 값만 믿지 않고 서버와 DB에서 최종 확인합니다.

## 로그인 흐름

```text
login.html
→ POST /login (empId, empPassword)
→ Spring Security
→ EmployeeUserDetailsService
→ EmployeeRepository.findByEmpId()
→ sgtransit_employee 조회
→ BCrypt 비밀번호 및 emp_status 비교
→ 성공 시 로그인 세션 생성
→ emp_role=U이면 메인 화면 / 로 이동
→ emp_role=A이면 관리자 화면 /admin 으로 이동
```

로그인 폼의 `name`은 보안 설정과 다음처럼 일치해야 합니다.

```text
아이디: empId
비밀번호: empPassword
```

회원가입할 때 입력한 비밀번호 원문은 DB에 저장하지 않습니다.
DB의 `emp_password`에는 BCrypt 해시가 저장되고 로그인 시 해시 비교가 수행됩니다.

## 주요 파일

```text
controller/PhoneVerificationController.java
service/PhoneVerificationService.java
sms/SolapiSmsSender.java
domain/PhoneVerification.java
repository/PhoneVerificationRepository.java
service/EmployeeUserDetailsService.java
config/SecurityConfig.java
templates/member/signup.html
templates/member/login.html
static/js/signup.js
static/js/login.js
static/css/login.css
controller/ComplaintController.java
templates/complaint/create.html
static/js/complaint.js
static/css/complaint.css
service/ComplaintService.java
domain/Complaint.java
domain/ComplaintAttachment.java
repository/ComplaintRepository.java
repository/ComplaintAttachmentRepository.java
database/complaint-schema.sql
database/schema.sql
config/RoleBasedAuthenticationSuccessHandler.java
controller/AdminController.java
service/AdminComplaintService.java
domain/ComplaintAnswer.java
repository/ComplaintAnswerRepository.java
templates/admin/dashboard.html
templates/admin/complaints.html
templates/admin/notices.html
templates/admin/members.html
templates/admin/fragments.html
static/css/admin.css
static/js/admin.js
database/admin-answer-schema.sql
database/admin-answer-attachment-schema.sql
```

## 실행

```powershell
.\gradlew.bat bootRun
```

브라우저:

```text
http://localhost:8080/signup
```

## 운영 환경에서 추가할 항목

- IP 단위 요청 횟수 제한
- CAPTCHA
- 오래된 인증내역 정기 삭제
- SMS 발송 결과 웹훅 처리
- API Key를 Secret Manager 또는 서버 환경변수로 관리
- HTTPS 적용

현재 구현에도 휴대전화 번호별 60초 재발송 제한이 있지만, 공개 서비스에서는
문자 발송 비용 악용 방지를 위해 IP 제한과 CAPTCHA를 추가해야 합니다.
