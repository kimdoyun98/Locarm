# Locarm


## 프로젝트 소개
 대중교통을 이용하여 목적지로 이동할 때 자거나, 음악을 듣다 보면 목적지를 지나쳐버리는 경우가 있습니다.
 
 또는 지나치지 않으려 잠을 제대로 자지 못하고 중간 중간에 위치를 확인하는 일이 허다합니다.

 그런 경우를 대비하여 목적지 인근 몇 km에 도달 시 **알람 및 진동**으로 **사용자에게 미리 알려주어 목적지까지 편히 쉬면서 도달 할 수 있도록** 만든 앱입니다.
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
`Kotlin` `MVVM` `Databinding` `ViewModel` `LiveData` `Flow` `Room` `Retrofit2` `Kotlinx-Serialization` `Paging3` `Coroutine` `Naver Map`
<br>
<br>

## 동작 시퀀스 다이어그램
<img src="https://github.com/user-attachments/assets/8c4770e8-f047-4907-ac71-6b328079fbea" width=800 />    
<br>    

## 권한 로직 다이어그램
<img src="https://github.com/user-attachments/assets/a8179141-ff62-4a4f-8ad1-28f451480772" width=800 /> 


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

