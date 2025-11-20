# Gemini Project Log - k6-scenario-builder

이 문서는 AI 에이전트(Gemini)가 `k6-scenario-builder` 프로젝트에서 수행한 주요 작업 및 변경 사항을 기록합니다. 향후 컨텍스트를 유지하고 작업을 계속하는 데 사용됩니다.

## 개발 규칙
1. 클린 코드를 지향하고, 객체지향 5대 원칙(SOLID) 준수
2. 도메인 중심 설계가 어울리는 경우 도메인 모델 패턴 사용, 데이터 중심이라면 트랜잭션 스크립트 패턴 사용
3. 레이어 간 철저한 DTO 분리로 응집도는 높이고 결합도는 낮춤
4. 디자인 패턴을 활용한 유지보수하기 용이한 코드를 작성한다
5. 무조건 코드 수정하지 말고, 수정 방향을 확인받고 진행
6. **진행 사항은 GEMINI.md 파일에 다음 세션과의 연속성을 위해 자세하게 정리**
7. docker compose 기반의 프로젝트로 구성한다7. docker compose 기반의 프로젝트로 구성한다7. docker compose 기반의 프로젝트로 구성한다7. docker compose 기반의 프로젝트로 구성한다7. docker compose 기반의 프로젝트로 구성한다7. docker compose 기반의 프로젝트로 구성한다7. docker compose 기반의 프로젝트로 구성한다
8. unit test 는 필수로 작성한다. (그 외 테스트는 지시에 의해 작성한다.)
9. 지나친 주석은 작성하지 않으며, 주석 및 텍스트 작성 시, 이모지는 사용하지 않고 가독성 좋게 작성한다.

## 프로젝트 목표

기존의 "load-test-toy" 프로젝트를 발전시켜, 어떤 Spring Boot 애플리케이션이든 쉽게 부하 테스트 시나리오를 생성하고 실행할 수 있는 라이브러리 및 도구를 구축하는 것입니다.

---

## 로드맵 (Phase 1 시작)

### **Phase 1: 핵심 엔진 구축 - API 분석 및 k6 함수 라이브러리 생성**

이 단계의 목표는 Spring Boot 컨트롤러 코드를 자동으로 분석하여, 각 API 엔드포인트에 해당하는 k6 함수들이 들어있는 JavaScript 라이브러리 파일을 생성하는 것입니다.

1.  **프로젝트 구조 변경 (멀티 모듈):**
    *   `load-test-core`: 코드 분석, 스크립트 생성 등 핵심 로직을 담을 라이브러리 모듈.
    *   `load-test-gradle-plugin`: 커스텀 Gradle Task를 제공할 플러그인 모듈.
    *   `sample-app`: 개발된 라이브러리와 플러그인을 테스트할 샘플 Spring Boot 애플리케이션 모듈.

2.  **API 엔드포인트 분석기 개발:**
    *   **Annotation Processor** 기술을 사용하여, 컴파일 시점에 `@RestController`, `@GetMapping` 등의 어노테이션을 분석합니다.
    *   분석된 API 정보(URL, HTTP Method, 파라미터 등)를 중간 결과물인 메타데이터 파일(예: `build/api-meta.json`)로 생성합니다.

3.  **k6 함수 생성기 개발:**
    *   API 메타데이터 파일을 읽어, 각 컨트롤러별로 k6 함수 라이브러리 파일(예: `user-controller.js`)을 동적으로 생성합니다. 각 파일은 API 호출 함수들을 `export` 합니다.

4.  **Gradle 플러그인 구현:**
    *   `generateK6Scripts`라는 Task를 만들어, 위 2, 3번 과정을 한 번에 실행할 수 있도록 합니다.

---

### **Phase 2: 시나리오 빌더 UI 개발**

이 단계의 목표는 1단계에서 생성된 함수들을 사용자가 시각적으로 조립하여 테스트 시나리오를 만들 수 있는 웹 인터페이스를 구축하는 것입니다.

1.  **UI 프로젝트 생성:**
    *   React, Vue 등 최신 프레임워크를 사용하여 시나리오 빌더 UI 프로젝트를 시작합니다.

2.  **함수 팔레트(Palette) 구현:**
    *   1단계에서 생성된 API 메타데이터 파일을 읽어, 사용 가능한 모든 API 함수 목록을 UI에 표시합니다.

3.  **시나리오 조립 캔버스 구현:**
    *   사용자가 함수 목록에서 원하는 함수를 드래그 앤 드롭하거나 클릭하여 테스트 순서를 구성할 수 있는 작업 공간을 만듭니다. (예: `createUser` 실행 -> 1초 대기 -> `getUserById` 실행)

4.  **파라미터 입력 UI 구현:**
    *   시나리오에 추가된 각 함수 블록에 대해, 필요한 파라미터(Request Body, Path Variable 등)를 입력할 수 있는 UI를 제공합니다.

5.  **시나리오 저장/로드:**
    *   사용자가 만든 시나리오를 JSON 형태로 저장하고 불러올 수 있는 기능을 구현합니다.

---

### **Phase 3: 테스트 실행 엔진 연동**

이 단계의 목표는 시나리오 빌더에서 만든 시나리오를 실제로 실행하고 결과를 확인하는 것입니다.

1.  **최종 스크립트 생성기 개발:**
    *   UI로부터 시나리오 JSON을 전달받아, k6 함수 라이브러리를 `import`하고 시나리오 순서대로 함수를 호출하는 **최종 실행용 k6 스크립트**를 동적으로 생성하는 백엔드 서비스를 개발합니다.

2.  **k6 실행 서비스 연동:**
    *   기존에 만들어 둔 `K6ControlService`를 활용하여, 1단계에서 생성된 최종 스크립트를 VUser, RPS 등과 함께 실행하도록 연동합니다.

3.  **결과 표시 UI 연동:**
    *   k6 테스트 실행 결과를 다시 UI로 가져와 사용자에게 보여줍니다.

---

### **Phase 4: 고급 기능 및 고도화**

1.  **데이터 연관관계(Correlation) 처리:**
    *   한 API의 응답(예: `createUser`의 `userId`)을 추출하여 다음 API의 요청 파라미터로 사용할 수 있는 기능을 시나리오 빌더에 추가합니다.
2.  **응답 검증(Assertions) 기능:**
    *   k6의 `check()` 함수처럼, API 응답의 상태 코드나 내용이 올바른지 검증하는 로직을 UI에서 추가할 수 있도록 합니다.
3.  **환경 관리 기능:**
    *   테스트 대상 서버(local, dev, staging)를 쉽게 변경할 수 있는 UI를 추가합니다.
4.  **라이브러리 패키징 및 배포:**
    *   개발된 Gradle 플러그인과 라이브러리를 실제 다른 프로젝트에서 `implementation(...)`으로 추가하여 사용할 수 있도록 패키징하고 배포합니다.

---

## **현재 진행 상황 (Phase 1 - 프로젝트 구조 변경 완료)**

### **2025년 11월 18일**

-   **새 프로젝트 생성**: `~/projects/k6-scenario-builder` 디렉토리 생성.
-   **Gradle 멀티 모듈 설정**:
    -   `settings.gradle` 파일 생성 (`rootProject.name = 'k6-scenario-builder'`, `include 'load-test-core'`, `include 'load-test-gradle-plugin'`, `include 'sample-app'`).
    -   루트 `build.gradle` 파일 생성 (Java 17, 공통 그룹/버전/저장소 설정).
    -   각 서브 프로젝트 (`load-test-core`, `load-test-gradle-plugin`, `sample-app`)의 디렉토리 구조 및 빈 `build.gradle` 파일 생성.
-   **Java 버전 설정**: 회사 프로젝트와의 호환성을 위해 모든 서브 프로젝트의 Java 버전을 17로 설정.

### **2025년 11월 20일**

-   **Gradle 빌드 환경 설정 완료**: Docker를 이용한 멀티모듈 Gradle 프로젝트 빌드 환경을 성공적으로 구축. `gradlew` 스크립트 부재, 의존성 관리 문제, 중복 파일 오류 등 여러 빌드 관련 문제를 해결함.
-   **API 엔드포인트 분석기 개발 완료 (Annotation Processor)**:
    -   `load-test-core` 모듈에 `ApiAnalyzerProcessor` (Annotation Processor) 개발 완료.
    -   `AutoService`를 이용하여 프로세서 자동 등록 및 실행 확인 완료.
    -   `sample-app` 모듈의 Spring Boot 컨트롤러에 대한 메타데이터(`api-meta.json`)를 성공적으로 추출 및 생성 확인 완료.
    -   API 메타데이터를 저장하기 위한 `ApiControllerInfo`, `ApiMethodInfo`, `ApiParameterInfo` 데이터 클래스 정의 완료.
    -   **테스트 코드 작성 완료**: `ApiAnalyzerProcessorTest`를 작성하여 `ApiAnalyzerProcessor`의 핵심 기능(Spring Boot 컨트롤러의 API 메타데이터 추출 및 `api-meta.json` 생성)을 검증하는 테스트 코드를 작성하고 성공적으로 실행 확인 완료.

### **2025년 11월 20일 (오후)**

-   **테스트 코드 컴파일 오류 해결**: `ApiAnalyzerProcessorTest`의 문법 오류를 수정했습니다.
-   **빌드 실패 문제 해결**: Gradle 빌드 시 파일 권한 문제로 인해 발생하던 빌드 실패 현상을 해결했습니다. 이로써 프로젝트가 성공적으로 빌드되는 것을 확인했습니다.

### **2025년 11월 20일 (저녁)**

-   **Java 및 Spring Boot 버전 업그레이드**: 프로젝트의 Java 버전을 21로, Spring Boot 버전을 3.3.5로 업그레이드했습니다.
-   **Gradle 빌드 구성 수정**: 버전 업그레이드에 따른 빌드 문제를 해결하기 위해 각 모듈의 `build.gradle` 파일을 수정하고 의존성을 정리했습니다.

### **2025년 11월 20일 (심야)**

-   **k6 함수 생성기 개발 (`K6ScriptGenerator.java`):**
    *   `api-meta.json` 메타데이터 파일을 읽어 k6 JavaScript 파일(예: `simple-controller.js`)을 생성하는 핵심 로직 구현.
    *   생성된 파일명의 중복(패키지명 포함) 문제를 해결하여 단순 클래스명 기반의 파일명(`simple-controller.js`)으로 생성하도록 수정.
    *   생성된 k6 스크립트의 개행 및 문자열 포맷팅 문제 해결 중. 테스트 코드와의 불일치로 인해 여러 차례 수정이 발생했습니다. (현재 테스트 실패)
-   **k6 함수 생성기 테스트 (`K6ScriptGeneratorTest.java`):**
    *   `K6ScriptGenerator`의 기능을 검증하기 위한 테스트 코드 작성.
    *   생성된 스크립트와 예상 스크립트 간의 개행 문자 불일치로 인해 많은 디버깅 작업 진행 중. `assertEquals` 실패 원인 파악을 위해 상세한 문자 단위 비교 로직을 추가하는 등의 노력을 기울였으며, 현재 테스트는 여전히 실패하고 있습니다.
-   **Gradle 빌드 환경 개선:**
    *   `K6ScriptGenerator`를 직접 실행하고 클래스패스를 디버깅하기 위해 `load-test-core/build.gradle`에 `printClasspath` 태스크 추가 (이후 `main` 메서드 제거와 함께 정리됨).
    *   `load-test-gradle-plugin` 모듈의 `build.gradle` 파일에 `groovy` 플러그인 및 `gradleApi()` 의존성 추가.
    *   `generateK6Scripts` Gradle Task의 초기 구조 정의.
    *   `sample-app` 모듈의 `build.gradle` 파일에서 Spring Boot 플러그인 버전 및 `io.spring.dependency-management` 플러그인 중복 적용 문제 해결.
    *   다양한 `build.gradle` 파일에서 발생한 의존성 해결 및 문법 오류 수정.

