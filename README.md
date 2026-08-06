# OTD PointShop Backend  
> Spring Boot 기반 포인트 상점(Reward Shop) 백엔드 서비스
> 기존 URL : [supii-dev/OTD_MSA_Back_User](https://github.com/supii-dev/OTD_MSA_Back_User)

## 프로젝트 개요
OTD PointShop은 OTD(OneToDay) MSA 시스템의 "포인트 상점 모듈"입니다.  
사용자는 포인트를 충전하여 상품을 구매할 수 있으며,  
관리자는 상품·카테고리 관리 및 월별 통계 확인이 가능합니다.  

> 본 모듈은 `OTD_MSA_Back_User`, `OTD_MSA_Back_PointShop`, `OTD_MSA_Front` 로 구성된 MSA 아키텍처 중 **PointShop 백엔드 서비스**에 해당합니다.

## 기술 스택
| 구분 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.x |
| **Language** | Java 17 |
| **Persistence** | Spring Data JPA, MyBatis |
| **Database** | MariaDB |
| **Auth** | Session-based Authentication |
| **Build Tool** | Gradle |
| **API Docs** | Swagger (Springdoc OpenAPI 2.x) |
| **File Upload** | MultipartFile, UUID FileName |
| **ETC** | Lombok, ModelMapper, ExceptionHandler |

## 프로젝트 구조
com.otd.otd_pointshop
├── application/
│ ├── point/ # 포인트 아이템 CRUD (관리자)
│ ├── purchase/ # 구매 내역 관리
│ ├── recharge/ # 포인트 충전 내역 및 잔액 관리
│ └── stats/ # 통계 대시보드 (월별·유저별·카테고리별)
│
├── entity/ # JPA Entity 클래스
├── repository/ # JPA + MyBatis Repository
├── service/ # 비즈니스 로직
├── controller/ # REST API Controller
├── model/ (dto) # Request / Response DTO
├── exception/ # CustomException, GlobalHandler
├── config/ # CORS, Swagger Config
└── resources/
├── mapper/ # MyBatis Mapper XML
├── static/upload/ # 이미지 업로드 디렉토리
└── application.yml # 환경 설정

## 핵심 아키텍처 개념
### 1. 계층 구조 설계 (Clean Architecture 기반)
- Controller → Service → Repository → Entity
- DTO를 통한 계층 간 데이터 전달 (`Request`, `Response` 분리)
- GlobalExceptionHandler로 예외 메시지 일관화

### 2. 파일 업로드 처리
- 이미지 파일 저장 시 UUID + 원본 확장자 유지
- 저장 경로: `static/upload/pointshop/`
- `Files.createDirectories()`를 통해 경로 자동 생성
- DB에는 파일명만 저장 (`point_item_image`)

```java
String fileName = UUID.randomUUID() + "." + ext;
Path savePath = Paths.get(uploadDir, fileName);
Files.copy(image.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
```

### 3. JPA + Mybatis 병행
- CRUD 중심 로직은 JPA
- 통계/집계/조인은 MyBatis (mapper XML) 로 최적화
- 예시: StatsRepository → JPQL / MyBatis 동시 활용

### 4. 세션 기반 인증
- JWT 없이 Spring Session 통한 로그인 상태 유지
- @AuthenticationPrincipal 또는 HttpSession 유저 식별

### 5. 통계 모듈 (StatsService)
- 월별 충전·구매 통계
- 카테고리별 구매 비율
- 유저별 TOP 10  랭킹
- DTO 예시 : MonthlySummaryStatsRes, CategoryStatsRes, UserTopRes

### 6. Database ERD
User (user_id PK)
 ├─< RechargeHistory (recharge_id PK)
 ├─< PurchaseHistory (purchase_id PK)
 └─< PointItem (point_id PK)
       ├─< PointImage (image_id PK)
       └─< PointCategory (category_id PK)

### 7. 주요 엔티티
#### 1. PointItem
필드	        타입	            설명
pointId	      Long	            포인트 상품 ID
itemName	    String	          상품명
itemPrice	    Integer	          상품 가격
category	    PointCategory	    카테고리 (ManyToOne)
images	      List<PointImage>	상품 이미지 목록

#### 2. RechargeHistory
필드	        타입	            설명
rechargeId	  Long	            충전 내역 ID
user	        Long	            사용자
amount	      Integer	          충전 포인트
rechargeAt	  LocalDateTime	    충전 일시

#### 3. PurchaseHistory
필드	        타입	            설명
purchaseId	  Long	            구매 내역 ID
user	        Long	            구매자
point	        PointItem	        구매 상품
purchaseTime	LocalDateTime	    구매 일시

### 8. 주요 API 명세
구분	                메서드	    URL	                                  설명
포인트 아이템 목록	    GET	        /OTD/pointshop/list	                  전체 상품 목록
아이템 등록 (Admin)	  POST	      /OTD/pointshop/add	                  상품 등록 (multipart/form-data)
아이템 수정	          PUT	        /OTD/pointshop/{id}	                  상품 정보 수정
아이템 삭제	          DELETE      /OTD/pointshop/{id}  	                상품 삭제
충전 내역 등록	        POST	      /OTD/pointshop/recharge	              포인트 충전 등록
잔액 조회	            GET	        /OTD/pointshop/recharge/balance	      사용자 포인트 잔액
구매 등록	            POST	      /OTD/pointshop/purchase/{pointId}	    상품 구매
구매 내역 조회	        GET	        /OTD/pointshop/purchase/user/{userId}	유저별 구매 내역
월별 통계	            GET	        /OTD/pointshop/admin/stats/summary	  월별 충전·구매 통계
카테고리별 통계	      GET	        /OTD/pointshop/admin/stats/category	  카테고리별 구매 비율
Top 10 통계	          GET	        /OTD/pointshop/admin/stats/top	      TOP10 충전·구매 유저

### 9. 예외 처리 구조
클래스	                  역할
CustomException	          비즈니스 로직 예외 클래스
ErrorCode	                에러 코드 및 HTTP 상태 정의
GlobalExceptionHandler	  전역 예외 처리

### 10. 향후 개선 계획
- 관리자 Role 기반 접근 제어 강화 (Spring Security)
- 상품 재고 관리 기능 추가
- 구매 취소 / 환불 로직 추가
- AWS S3 파일 업로드로 전환
- Redis 캐시 기반 통계 성능 개선
