# Locarm

<img width="758" height="451" alt="Image" src="https://github.com/user-attachments/assets/583044a2-2df6-4c3d-83a7-4dfa9ae5ea8f" />

## 프로젝트 소개
 대중교통을 이용하여 목적지로 이동할 때 자거나, 음악을 듣다 보면 목적지를 지나쳐버리는 경우가 있습니다.
 
 또는 지나치지 않으려 잠을 제대로 자지 못하고 중간 중간에 위치를 확인하는 일이 허다합니다.

 그런 경우를 대비하여 목적지 인근 몇 km에 도달 시 **알람 및 진동**으로 **사용자에게 미리 알려주어 목적지까지 편히 쉬면서 도달 할 수 있도록** 만든 앱입니다.

<br>    


## 핵심 기능
* 목적지 검색 기능
  * 주소기반산업지원서비스 Open API 사용, 주소를 검색하여 해당 주소의 이름과 좌표를 얻어 Naver Map에 표시 할 수 있게 구현
  * 주소를 잘 모르는 경우 Naver Map에서 수동으로 선택할 수 있도록 구현
* 자주 가능 장소 즐겨찾기
  * Room DB에 즐겨찾기 주소 저장
* 목적지 등록 시 백그라운드에서 실시간 위치 추적
  * Foreground Notification으로 실시간 남은 거리 확인
  * 남은 거리가 설정한 거리까지 도달하면 사용자가 종료하기 전까지 진동 및 알림
 <br>


## Tech Stack
`Kotlin` `MVVM` `Databinding` `ViewModel` `LiveData` `Flow` `Room` `Retrofit2` `Kotlinx-Serialization` `Paging3` `Coroutine` `Naver Map` `GithubActions`
<br>    

* Gson → Kotlinx-Serialization
  * Gson의 경우 값이 Null로 올 경우를 대비해 Nullable 처리
  * Kotlinx-Serialization의 경우 Default Value를 통해 Null이 아닌 값을 전달하여 NPE 위험 감소
  * 리플렉션을 사용하는 Gson과 달리 Kotlinx-Serialization는 리플렉션을 사용하지 않아 성능을 향상
 
* LiveData → Flow
  * LiveData의 경우 생명주기를 자동으로 관리해주기 때문에 간편함
  * Repository의 데이터를 Activity 뿐만 아니라 Service에서도 사용
  * 상태 데이터를 조합하여 사용하는 경우에 LiveData는 제한적인 경우가 많음
  * 따라서 여러 컴포넌트에서 유동적으로 사용할 수 있고, 여러 데이터를 조합 및 추가적인 연산을 수행하기 적합한 Flow로 리팩토링
 
* NaverMap
  * 월 무료 이용 횟수가 가장 많다.(월 1억회, 카카오는 일 300,000회)
  * 한국에서 사용하는 앱으로 사용자에게 있어 가장 친화적인 Map UI

<br>    

## 개발 기간
프로젝트 시작 (23.11 ~ 23.12)
* 목적지 검색
* 즐겨찾기
* 백그라운드에서 위치 추적

<br>    

리팩토링 (2025.04.27 ~ 2025.05.05)
* 목적지 검색 Paging 적용
* 클래스 분리 및 간소화
* 목적지까지 남은 거리 Notification 추가
* 위치 추적 상태에 따른 버튼 UI 변경

<br>    

UI 변경 및 리팩토링 (2026.03.06 ~ 2026.03.17)
* Home, 즐겨찾기, 목적지 검색, Foreground Notification UI 변경
* LocarmSnackBar & TopStackingNotification 구현
* Paging의 RemoteMediator 및 Room DB 캐싱 적용하여 API 호출 최소화
* AppContainer로 DI 적용
* 위치 권한 및 위치 설정 활성화 여부에 따른 기능 제한 및 알림 추가
* 다크모드 비활성화
 <br>
<br>

## 동작 시퀀스 다이어그램
<img src="https://github.com/user-attachments/assets/8c4770e8-f047-4907-ac71-6b328079fbea" width=800 />    
<br>    

## 권한 로직 다이어그램
<img src="https://github.com/user-attachments/assets/a8179141-ff62-4a4f-8ad1-28f451480772" width=800 /> 

<br>    

## 🔧 API 호출, 페이징 및 Room DB 캐싱
- 문제
    - 주소를 검색하면 결과 값이 몇 개든 모두 불러옴
    - 동일한 쿼리에 대한 검색도 매번 API 호출
- 해결
    - Paging3 라이브러리를 통해 페이지 별로 10개씩 데이터 호출
    - RemoteMediator를 통해 호출 한 데이터는 Room DB에 저장
- 결과
    - 검색한 주소는 10개씩 페이지 별로 호출
    - 동일한 주소 쿼리 및 페이지에 대해서 Room DB에 저장 된 데이터 호출로 API 호출 최소화
<br>    

## ⚙ In- App Notification 구현 [(코드)](https://github.com/kimdoyun98/Locarm/blob/main/app/src/main/java/com/project/locarm/ui/view/LocarmNotification.kt)
View에서 제공하는 기존 SnackBar의 경우 Action 버튼 하나만 제공하여 두 개의 버튼을 사용하고자 할 때 제한되는 경우가 있다. 이에 따라 사용자 알림 View인 LocarmNotification을 구현하여 사용하도록 하였다.

**과정**
- 추상 클래스(LocarmNotification)를 만들어 공통 로직을 구현
    - ViewBinding 객체
    - show(), dismiss() 함수 구현
    - show, dismiss에 관한 Animation
- LocarmSnackBar와 TopStackingNotification 클래스에서 추상 클래스를 상속받아 각각의 Notification에 맞는 기능 구현
<img width="250" alt="Image" src="https://github.com/user-attachments/assets/d336de4d-3c65-4f9b-8071-e824997476b2" />
<img width="250" alt="Image" src="https://github.com/user-attachments/assets/a209a56c-5665-4739-bed1-28d44dbe3d8d" />
<img width="250"  alt="Image" src="https://github.com/user-attachments/assets/5abe0f25-13de-45ac-ad1d-cf72897dd841" />
<br>    

## Github Actions로 release 업데이트 시 자동 AAB 파일 및 APK 파일 생성
```
git tag v1.0.3
git push origin v1.0.3
```
* main branch에 v로 시작하는 tag가 오면 WorkFlow 실행
* 앱의 마지막 Google Play 배포 시점에 적용하여 통합, 테스트와 관련된 자동화는 생략하고 배포 파일만 생성

## Learn
* [Service를 이용한 백그라운드 작업](https://snaildeveloper.tistory.com/121)
* [실시간 위치 추적](https://snaildeveloper.tistory.com/123)
* [Retrofit2 Http 네트워크 통신](https://snaildeveloper.tistory.com/127)
* [Json Converter 라이브러리](https://snaildeveloper.tistory.com/128)
* [DI 라이브러리 없이 DI 구현](https://snaildeveloper.tistory.com/156)
<br>

 ## GIF & ScreenShot
<img src="https://github.com/user-attachments/assets/f78336ae-a6e5-4820-9425-686fa27958d4" width=250 />
<img src="https://github.com/user-attachments/assets/727be8b2-52cd-4bab-92a8-dc31521f5e7f" width=250 />
<img src="https://github.com/user-attachments/assets/bb77a39c-5c1f-4875-ad72-3d658ac30caa" width=250 />
<img src="https://github.com/user-attachments/assets/e906a8b5-f197-47e6-a14d-9e52a8dc0da5" width=250 />
<img src="https://github.com/user-attachments/assets/4314c9e1-c9b7-4d1e-aa8f-b26470055e13" width=250 />
<img src="https://github.com/user-attachments/assets/649b62cb-29d2-48f1-8854-846a9922e032" width=250 />
<img src="https://github.com/user-attachments/assets/2d3f5810-c33a-4603-ab04-c19e7161a4e0" width=250 />

