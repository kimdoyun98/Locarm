# Locarm


## 프로젝트 소개
 대중교통을 이용하여 목적지로 이동할 때 자거나, 음악을 듣다 보면 목적지를 지나쳐버리는 경우가 있습니다.
 
 또는 지나치지 않으려 잠을 제대로 자지 못하고 중간 중간에 위치를 확인하는 일이 허다합니다.

 그런 경우를 대비하여 목적지 인근 몇 km에 도달 시 **알람 및 진동**으로 **사용자에게 미리 알려주어 목적지까지 편히 쉬면서 도달 할 수 있도록** 만든 앱입니다.
 <br>


## 개발 기간
 * 23.11.14 ~ 23.11.21 개발
 * 25.04.27 ~ 25.05.05 리팩토링
 <br>


## 핵심 기능
* 목적지 검색 기능
  * 주소기반산업지원서비스 Open API 사용, 주소를 검색하여 해당 주소의 이름과 좌표를 얻어 Naver Map에 표시 할 수 있게 구현
  * 주소를 잘 모르는 경우 Naver Map에서 수동으로 선택할 수 있도록 구현
  * 주소 검색 결과는 Paging3를 이용하여 한 번에 많은 양의 데이터가 넘어오지 않도록 구현
* 자주 가능 장소 즐겨찾기
  * Room DB에 즐겨찾기 주소 저장
* 목적지 등록 시 백그라운드에서 실시간 위치 추적
  * Foreground Notification으로 실시간 남은 거리 확인
  * 남은 거리가 설정한 거리까지 도달하면 사용자가 종료하기 전까지 진동 및 알림
 <br>

## Tech Stack
`Kotlin` `MVVM` `Jetpack` `AAC` `Databinding` `LiveData` `ViewModel` `Room DB` `Retrofit2` `Paging3` `Coroutine` 
<br>


## Learn
* [Service를 이용한 백그라운드 작업](https://snaildeveloper.tistory.com/121)
* [실시간 위치 추적](https://snaildeveloper.tistory.com/123)
* [Retrofit2 Http 네트워크 통신](https://snaildeveloper.tistory.com/127)
* [Json Converter 라이브러리](https://snaildeveloper.tistory.com/128)
<br>

 ## GIF & ScreenShot
<img src="https://github.com/user-attachments/assets/1ef5e439-34b6-4475-ae5e-39102e754657" width=250 />
<img src="https://github.com/user-attachments/assets/259fcc09-7060-4386-8a5f-1674e1e178cf" width=250 />
<img src="https://github.com/user-attachments/assets/1c19d48c-97bc-4e18-933e-f34eed23204a" width=250 />

