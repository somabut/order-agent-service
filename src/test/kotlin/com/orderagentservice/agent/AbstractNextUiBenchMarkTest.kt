//package com.orderagentservice.agent
//
//import com.orderagentservice.agent.model.dto.AgentUiDto
//import com.orderagentservice.agent.model.dto.UiComponentDto
//
//abstract class AbstractNextUiBenchMarkTest {
//    protected val backBenchList = listOf<List<AgentUiDto>>(
//        listOf(
//            AgentUiDto(x = 132, y = 288, title = "불고기버거 5200원"),
//            AgentUiDto(x = 244, y = 401, title = "치즈버거 4800원"),
//            AgentUiDto(x = 392, y = 615, title = "새우버거 5600원"),
//            AgentUiDto(x = 118, y = 745, title = "다음으로"),
//            AgentUiDto(x = 356, y = 302, title = "코카콜라 1700원"),
//            AgentUiDto(x = 276, y = 842, title = "제로 콜라 1800원"),
//            AgentUiDto(x = 462, y = 418, title = "취소"),
//            AgentUiDto(x = 104, y = 918, title = "사이다 1700원"),
//            AgentUiDto(x = 378, y = 531, title = "환타 오렌지 1700원")
//        ),
//        listOf(
//            AgentUiDto(x = 160, y = 320, title = "감자튀김 2500원"),
//            AgentUiDto(x = 218, y = 486, title = "어니언링 2800원"),
//            AgentUiDto(x = 402, y = 608, title = "치즈스틱 3000원"),
//            AgentUiDto(x = 130, y = 736, title = "선택완료"),
//            AgentUiDto(x = 368, y = 295, title = "아메리카노 3000원"),
//            AgentUiDto(x = 270, y = 858, title = "라떼 3500원"),
//            AgentUiDto(x = 455, y = 425, title = "이전으로"),
//            AgentUiDto(x = 110, y = 930, title = "핫초코 3200원"),
//            AgentUiDto(x = 384, y = 540, title = "아이스티 2800원")
//        ),
//        listOf(
//            AgentUiDto(x = 140, y = 300, title = "콤보A 8900원"),
//            AgentUiDto(x = 225, y = 470, title = "콤보B 9900원"),
//            AgentUiDto(x = 395, y = 600, title = "콤보C 10900원"),
//            AgentUiDto(x = 124, y = 728, title = "담기"),
//            AgentUiDto(x = 360, y = 310, title = "물 1000원"),
//            AgentUiDto(x = 280, y = 860, title = "오렌지주스 2500원"),
//            AgentUiDto(x = 460, y = 420, title = "취소"),
//            AgentUiDto(x = 108, y = 920, title = "사이드 추가 0원"),
//            AgentUiDto(x = 375, y = 535, title = "케첩 추가 0원")
//        ),
//        listOf(
//            AgentUiDto(x = 152, y = 312, title = "불고기 와퍼 7800원"),
//            AgentUiDto(x = 232, y = 482, title = "치킨버거 7400원"),
//            AgentUiDto(x = 398, y = 610, title = "더블치즈 9200원"),
//            AgentUiDto(x = 120, y = 740, title = "다음 단계"),
//            AgentUiDto(x = 362, y = 298, title = "콜라 라지 2200원"),
//            AgentUiDto(x = 272, y = 852, title = "콜라 미디엄 1900원"),
//            AgentUiDto(x = 458, y = 412, title = "이전"),
//            AgentUiDto(x = 106, y = 926, title = "레몬에이드 3200원"),
//            AgentUiDto(x = 380, y = 528, title = "밀크쉐이크 3900원")
//        ),
//        listOf(
//            UiComponentDto(x = 148, y = 318, title = "치즈 프라이 3200원"),
//            UiComponentDto(x = 216, y = 488, title = "스파이시 너겟 5900원"),
//            UiComponentDto(x = 404, y = 606, title = "해쉬브라운 2000원"),
//            UiComponentDto(x = 128, y = 734, title = "선택완료"),
//            UiComponentDto(x = 358, y = 304, title = "아이스 아메리카노 2800원"),
//            UiComponentDto(x = 268, y = 846, title = "카라멜 라떼 4200원"),
//            UiComponentDto(x = 452, y = 418, title = "취소"),
//            UiComponentDto(x = 112, y = 924, title = "우유 1500원"),
//            UiComponentDto(x = 382, y = 536, title = "복숭아 아이스티 3000원")
//        ),
//        listOf(
//            UiComponentDto(x = 142, y = 306, title = "베이컨 치즈버거 8400원"),
//            UiComponentDto(x = 228, y = 476, title = "불고기 라이스 7200원"),
//            UiComponentDto(x = 396, y = 604, title = "새우 텐더 6100원"),
//            UiComponentDto(x = 126, y = 732, title = "다음"),
//            UiComponentDto(x = 364, y = 300, title = "스프라이트 1800원"),
//            UiComponentDto(x = 278, y = 850, title = "제로 사이다 1900원"),
//            UiComponentDto(x = 456, y = 416, title = "이전"),
//            UiComponentDto(x = 114, y = 922, title = "자두에이드 3400원"),
//            UiComponentDto(x = 376, y = 532, title = "포테이토 라지 2800원")
//        ),
//        listOf(
//            UiComponentDto(x = 150, y = 314, title = "키즈 세트 6500원"),
//            UiComponentDto(x = 220, y = 484, title = "버거 세트 9800원"),
//            UiComponentDto(x = 400, y = 612, title = "치킨 세트 10500원"),
//            UiComponentDto(x = 122, y = 742, title = "다음으로"),
//            UiComponentDto(x = 366, y = 296, title = "콜라 리필 0원"),
//            UiComponentDto(x = 270, y = 856, title = "핫도그 3500원"),
//            UiComponentDto(x = 454, y = 414, title = "취소"),
//            UiComponentDto(x = 116, y = 928, title = "콘샐러드 2000원"),
//            UiComponentDto(x = 386, y = 538, title = "치킨너겟 4900원")
//        ),
//        listOf(
//            UiComponentDto(x = 138, y = 298, title = "페퍼로니 피자 15900원"),
//            UiComponentDto(x = 210, y = 472, title = "포테이토 피자 16900원"),
//            UiComponentDto(x = 392, y = 602, title = "하와이안 피자 14900원"),
//            UiComponentDto(x = 118, y = 738, title = "완료"),
//            UiComponentDto(x = 352, y = 292, title = "콜라 1.25L 2800원"),
//            UiComponentDto(x = 274, y = 848, title = "갈릭 디핑소스 500원"),
//            UiComponentDto(x = 460, y = 410, title = "취소"),
//            UiComponentDto(x = 108, y = 918, title = "핫소스 300원"),
//            UiComponentDto(x = 372, y = 526, title = "피클 추가 0원")
//        ),
//        listOf(
//            UiComponentDto(x = 146, y = 322, title = "모짜렐라 버거 8600원"),
//            UiComponentDto(x = 214, y = 492, title = "갈릭버거 7900원"),
//            UiComponentDto(x = 406, y = 608, title = "그릴드 치킨 8800원"),
//            UiComponentDto(x = 132, y = 736, title = "선택완료"),
//            UiComponentDto(x = 354, y = 306, title = "바닐라 쉐이크 3900원"),
//            UiComponentDto(x = 272, y = 854, title = "초코 쉐이크 3900원"),
//            UiComponentDto(x = 448, y = 420, title = "이전으로"),
//            UiComponentDto(x = 112, y = 934, title = "딸기 쉐이크 4200원"),
//            UiComponentDto(x = 382, y = 542, title = "아이스크림 콘 1500원")
//        ),
//        listOf(
//            UiComponentDto(x = 144, y = 316, title = "불고기 라지세트 11200원"),
//            UiComponentDto(x = 222, y = 486, title = "치즈 라지세트 10900원"),
//            UiComponentDto(x = 398, y = 606, title = "쉬림프 라지세트 11800원"),
//            UiComponentDto(x = 126, y = 744, title = "카트담기"),
//            UiComponentDto(x = 360, y = 300, title = "콜라 라지 2200원"),
//            UiComponentDto(x = 276, y = 852, title = "사이다 라지 2200원"),
//            UiComponentDto(x = 452, y = 414, title = "취소"),
//            UiComponentDto(x = 110, y = 926, title = "제로 콜라 라지 2300원"),
//            UiComponentDto(x = 378, y = 532, title = "아이스 아메리카노 2800원")
//        ),
//        listOf(
//            UiComponentDto(x = 136, y = 302, title = "후라이드 치킨 15900원"),
//            UiComponentDto(x = 230, y = 472, title = "양념 치킨 16900원"),
//            UiComponentDto(x = 394, y = 598, title = "간장 치킨 16900원"),
//            UiComponentDto(x = 120, y = 740, title = "다음으로"),
//            UiComponentDto(x = 358, y = 294, title = "순살 변경 1000원"),
//            UiComponentDto(x = 270, y = 850, title = "반반 변경 500원"),
//            UiComponentDto(x = 458, y = 408, title = "취소"),
//            UiComponentDto(x = 108, y = 920, title = "무 추가 500원"),
//            UiComponentDto(x = 374, y = 524, title = "콜라 1.5L 3000원")
//        ),
//        listOf(
//            UiComponentDto(x = 150, y = 326, title = "세트 업그레이드 1500원"),
//            UiComponentDto(x = 218, y = 490, title = "치즈 추가 500원"),
//            UiComponentDto(x = 402, y = 612, title = "패티 추가 1500원"),
//            UiComponentDto(x = 124, y = 748, title = "선택완료"),
//            UiComponentDto(x = 362, y = 306, title = "양상추 추가 300원"),
//            UiComponentDto(x = 274, y = 858, title = "피클 제거 0원"),
//            UiComponentDto(x = 456, y = 420, title = "취소"),
//            UiComponentDto(x = 114, y = 936, title = "케첩 추가 0원"),
//            UiComponentDto(x = 386, y = 544, title = "마요 제거 0원")
//        ),
//        listOf(
//            UiComponentDto(x = 142, y = 308, title = "브런치 세트 9900원"),
//            UiComponentDto(x = 224, y = 480, title = "팬케이크 세트 8900원"),
//            UiComponentDto(x = 396, y = 606, title = "에그머핀 세트 7900원"),
//            UiComponentDto(x = 118, y = 742, title = "다음"),
//            UiComponentDto(x = 360, y = 298, title = "오렌지 주스 2500원"),
//            UiComponentDto(x = 276, y = 854, title = "우유 1500원"),
//            UiComponentDto(x = 452, y = 416, title = "이전"),
//            UiComponentDto(x = 110, y = 928, title = "핫커피 2000원"),
//            UiComponentDto(x = 378, y = 536, title = "아이스커피 2200원")
//        ),
//        listOf(
//            AgentUiDto(x = 148, y = 318, title = "샐러드 베이컨 7200원"),
//            AgentUiDto(x = 212, y = 492, title = "샐러드 치킨 7500원"),
//            AgentUiDto(x = 404, y = 610, title = "샐러드 연어 8900원"),
//            AgentUiDto(x = 130, y = 746, title = "확인"),
//            AgentUiDto(x = 356, y = 304, title = "발사믹 드레싱 0원"),
//            AgentUiDto(x = 270, y = 862, title = "시저 드레싱 500원"),
//            AgentUiDto(x = 450, y = 418, title = "취소"),
//            AgentUiDto(x = 112, y = 932, title = "올리브 추가 700원"),
//            AgentUiDto(x = 384, y = 540, title = "크루통 추가 500원")
//        ),
//        listOf(
//            AgentUiDto(x = 140, y = 310, title = "불닭 라이스 6800원"),
//            AgentUiDto(x = 226, y = 488, title = "치킨 마요 6500원"),
//            AgentUiDto(x = 398, y = 608, title = "참치 마요 6200원"),
//            AgentUiDto(x = 122, y = 748, title = "확인"),
//            AgentUiDto(x = 362, y = 300, title = "계란 추가 1000원"),
//            AgentUiDto(x = 274, y = 860, title = "마요 추가 500원"),
//            AgentUiDto(x = 452, y = 420, title = "이전으로"),
//            AgentUiDto(x = 108, y = 934, title = "김치 추가 500원"),
//            AgentUiDto(x = 380, y = 544, title = "단무지 추가 300원")
//        )
//    )
//    protected val backAnswerList = listOf(
//        "다음으로",
//        "선택완료",
//        "담기",
//        "다음 단계",
//        "선택완료",
//        "다음",
//        "다음으로",
//        "완료",
//        "선택완료",
//        "카트담기",
//        "다음으로",
//        "선택완료",
//        "다음",
//        "확인",
//        "확인"
//    )
//
//    protected val paymentBenchList = listOf<List<UiComponentDto>>(
//        listOf(
//            UiComponentDto(x = 150, y = 320, title = "불고기버거 세트 6200원"),
//            UiComponentDto(x = 228, y = 495, title = "치즈버거 5700원"),
//            UiComponentDto(x = 410, y = 610, title = "새우버거 6300원"),
//            UiComponentDto(x = 370, y = 300, title = "카드를 넣어주세요"),
//            UiComponentDto(x = 280, y = 860, title = "콜라 1800원"),
//            UiComponentDto(x = 460, y = 420, title = "취소"),
//            UiComponentDto(x = 110, y = 930, title = "사이다 1700원"),
//            UiComponentDto(x = 380, y = 530, title = "제로콜라 1900원")
//        ),
//        listOf(
//            UiComponentDto(x = 142, y = 312, title = "결제하기"),
//            UiComponentDto(x = 220, y = 498, title = "아이스 아메리카노 3000원"),
//            UiComponentDto(x = 392, y = 612, title = "카푸치노 3500원"),
//            UiComponentDto(x = 126, y = 734, title = "선택완료"),
//            UiComponentDto(x = 354, y = 304, title = "토마토주스 3200원"),
//            UiComponentDto(x = 450, y = 418, title = "이전으로"),
//            UiComponentDto(x = 112, y = 924, title = "결제수단 선택"),
//            UiComponentDto(x = 384, y = 540, title = "마이페이지 이동")
//        ),
//        listOf(
//            UiComponentDto(x = 146, y = 318, title = "카드를 삽입해주세요"),
//            UiComponentDto(x = 216, y = 490, title = "햄버거 단품 4900원"),
//            UiComponentDto(x = 400, y = 606, title = "콜라 추가 1500원"),
//            UiComponentDto(x = 122, y = 738, title = "옵션 선택"),
//            UiComponentDto(x = 366, y = 296, title = "사이드 추가"),
//            UiComponentDto(x = 270, y = 856, title = "결제정보 확인"),
//            UiComponentDto(x = 454, y = 414, title = "뒤로가기"),
//            UiComponentDto(x = 116, y = 928, title = "현금결제는 불가합니다"),
//            UiComponentDto(x = 386, y = 538, title = "포인트 적립")
//        ),
//        listOf(
//            UiComponentDto(x = 144, y = 314, title = "카드결제 진행 중입니다"),
//            UiComponentDto(x = 224, y = 476, title = "불닭버거 세트 7900원"),
//            UiComponentDto(x = 390, y = 604, title = "쉐이크 초코 3500원"),
//            UiComponentDto(x = 118, y = 740, title = "다음"),
//            UiComponentDto(x = 362, y = 302, title = "입구에 꽃아주세요"),
//            UiComponentDto(x = 278, y = 848, title = "물 1000원"),
//            UiComponentDto(x = 458, y = 412, title = "이전으로"),
//            UiComponentDto(x = 108, y = 918, title = "결제"),
//            UiComponentDto(x = 376, y = 528, title = "사이드 선택")
//        ),
//        listOf(
//            UiComponentDto(x = 140, y = 306, title = "키오스크 이용 방법"),
//            UiComponentDto(x = 230, y = 478, title = "신용카드 삽입"),
//            UiComponentDto(x = 396, y = 608, title = "아이스티 레몬 2500원"),
//            UiComponentDto(x = 124, y = 742, title = "결제 확인"),
//            UiComponentDto(x = 360, y = 298, title = "감자튀김 추가 2000원"),
//            UiComponentDto(x = 274, y = 854, title = "결제 완료되었습니다"),
//            UiComponentDto(x = 452, y = 416, title = "취소"),
//            UiComponentDto(x = 110, y = 926, title = "쿠폰 적용"),
//            UiComponentDto(x = 378, y = 536, title = "카드결제 영수증 출력")
//        ),
//        listOf(
//            UiComponentDto(x = 120, y = 250, title = "불고기버거 세트 6800원"),
//            UiComponentDto(x = 215, y = 370, title = "치즈스틱 4300원"),
//            UiComponentDto(x = 300, y = 480, title = "코카콜라 라지 2300원"),
//            UiComponentDto(x = 105, y = 600, title = "신용카드로 결제하기"),
//            UiComponentDto(x = 340, y = 720, title = "물 1500원"),
//            UiComponentDto(x = 190, y = 890, title = "결제 취소"),
//            UiComponentDto(x = 470, y = 930, title = "에그 타르트 2900원")
//        ),
//        listOf(
//            UiComponentDto(x = 135, y = 270, title = "아이스 아메리카노 3000원"),
//            UiComponentDto(x = 225, y = 360, title = "핫초코 3500원"),
//            UiComponentDto(x = 130, y = 570, title = "버블티 4900원"),
//            UiComponentDto(x = 360, y = 680, title = "에이드 5500원"),
//            UiComponentDto(x = 410, y = 780, title = "다시 선택"),
//            UiComponentDto(x = 480, y = 950, title = "결제하기")
//        ),
//        listOf(
//            UiComponentDto(x = 110, y = 240, title = "딸기 스무디 5200원"),
//            UiComponentDto(x = 220, y = 330, title = "망고 주스 5000원"),
//            UiComponentDto(x = 290, y = 420, title = "오렌지 주스 4700원"),
//            UiComponentDto(x = 320, y = 600, title = "카드를 삽입해주세요"),
//            UiComponentDto(x = 170, y = 780, title = "뒤로 가기")
//        ),
//        listOf(
//            UiComponentDto(x = 100, y = 230, title = "스파게티 7200원"),
//            UiComponentDto(x = 200, y = 320, title = "피자 한 조각 2800원"),
//            UiComponentDto(x = 290, y = 410, title = "콜라 1500원"),
//            UiComponentDto(x = 130, y = 500, title = "사이다 1600원"),
//            UiComponentDto(x = 180, y = 770, title = "확인"),
//            UiComponentDto(x = 470, y = 860, title = "이전으로")
//        ),
//        listOf(
//            UiComponentDto(x = 115, y = 245, title = "불닭볶음면 4000원"),
//            UiComponentDto(x = 210, y = 335, title = "우유 1200원"),
//            UiComponentDto(x = 300, y = 425, title = "두유 1500원"),
//            UiComponentDto(x = 145, y = 515, title = "결제 방식 선택"),
//            UiComponentDto(x = 330, y = 605, title = "장바구니"),
//            UiComponentDto(x = 395, y = 695, title = "다음으로"),
//            UiComponentDto(x = 185, y = 785, title = "취소")
//        ),
//        listOf(
//            UiComponentDto(x = 125, y = 255, title = "핫도그 2200원"),
//            UiComponentDto(x = 230, y = 345, title = "아이스크림 3200원"),
//            UiComponentDto(x = 310, y = 435, title = "딸기 500원"),
//            UiComponentDto(x = 135, y = 525, title = "햄버거"),
//            UiComponentDto(x = 350, y = 615, title = "확인"),
//            UiComponentDto(x = 405, y = 705, title = "이전"),
//            UiComponentDto(x = 195, y = 795, title = "적립")
//        ),
//        listOf(
//            UiComponentDto(x = 140, y = 260, title = "토스트 3100원"),
//            UiComponentDto(x = 240, y = 350, title = "샌드위치 3700원"),
//            UiComponentDto(x = 320, y = 440, title = "카드결제 진행 중"),
//            UiComponentDto(x = 140, y = 530, title = "현금 결제"),
//            UiComponentDto(x = 370, y = 620, title = "카드를 삽입해주세요"),
//            UiComponentDto(x = 410, y = 710, title = "결제하기"),
//            UiComponentDto(x = 210, y = 800, title = "포인트 적립")
//        ),
//        listOf(
//            UiComponentDto(x = 130, y = 270, title = "라떼 4200원"),
//            UiComponentDto(x = 235, y = 360, title = "카라멜 마끼아또 4500원"),
//            UiComponentDto(x = 310, y = 450, title = "아포가토 4800원"),
//            UiComponentDto(x = 360, y = 630, title = "돌아가기"),
//            UiComponentDto(x = 420, y = 720, title = "결제하기")
//        ),
//        listOf(
//            UiComponentDto(x = 135, y = 275, title = "치킨버거 5700원"),
//            UiComponentDto(x = 240, y = 365, title = "감자튀김 2000원"),
//            UiComponentDto(x = 320, y = 455, title = "음료 리필 1000원"),
//            UiComponentDto(x = 160, y = 545, title = "카드결제"),
//            UiComponentDto(x = 370, y = 635, title = "현금"),
//            UiComponentDto(x = 430, y = 725, title = "쿠폰 사용")
//        ),
//        listOf(
//            UiComponentDto(x = 145, y = 280, title = "닭강정 6700원"),
//            UiComponentDto(x = 250, y = 370, title = "감자크로켓 2500원"),
//            UiComponentDto(x = 330, y = 460, title = "사이다 제로 1800원"),
//            UiComponentDto(x = 330, y = 460, title = "장바구니 1800원"),
//            UiComponentDto(x = 330, y = 460, title = "모두 취소 1800원"),
//            UiComponentDto(x = 440, y = 730, title = "확인"),
//            UiComponentDto(x = 230, y = 820, title = "되돌아가기")
//        )
//
//    )
//    protected val paymentAnswerList = listOf(
//        "카드를 넣어주세요",
//        "선택완료",
//        "카드를 삽입해주세요",
//        "입구에 꽃아주세요",
//        "신용카드 삽입",
//        "신용카드로 결제하기",
//        "결제하기",
//        "카드를 삽입해주세요",
//        "확인",
//        "다음으로",
//        "확인",
//        "카드를 삽입해주세요",
//        "결제하기",
//        "카드결제",
//        "확인"
//    )
//}