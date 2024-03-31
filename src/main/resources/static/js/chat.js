$(document).ready(function(){
    var socket = new SockJS("/ws-stomp"); // SockJS를 사용하여 웹소켓 연결 설정
    var stompClient = Stomp.over(socket); // Stomp를 사용하여 WebSocket 연결 설정

    $(".gesture-button").click(function(){
        var gesture = $(this).data("gesture");
        chooseGesture(gesture);
    });

    function chooseGesture(gesture) {
        // 선택한 제스처를 서버로 전송
        stompClient.send("/pub/chat", {}, username + ": " + gesture); // WebSocket을 통해 메시지 전송
        // 선택 창을 숨기고 채팅 입력을 다시 표시
        $("#gameChoices").hide();
        $("#msgArea").show();
    }

    let username = "";
    let gameAccepted = false;

    // 사용자 정보 가져오기
    $.ajax({
        type: "GET",
        url: "/chat/user",
        success: function(data) {
            // 요청이 성공하면 username을 설정
            username = data;
            console.log("현재 사용자:", username);
            // WebSocket 연결 시작
            startWebSocket();
        },
        error: function(xhr, status, error) {
            console.error("사용자 정보를 가져오는 중 오류 발생:", error);
            // 에러 처리 로직 추가
        }
    });

    function startWebSocket() {
        // WebSocket 연결 시작
        stompClient.connect({}, function(frame) {
            console.log('WebSocket 연결 성공:', frame);

            $("#button-send").on("click",(e)=>{
                if (!gameAccepted) {
                    send();
                } else {
                    // 게임이 진행 중이므로 채팅을 보낼 수 없음을 알림
                    alert("게임 중에는 채팅을 보낼 수 없습니다.");
                }
            });

            // 게임 요청 버튼 클릭 시 이벤트
            $("#game-request").on("click", function() {
                // 게임 요청 메시지를 상대방에게 전송
                let message = username + ":게임 요청";
                stompClient.send("/pub/chat", {}, message);
            });

            stompClient.subscribe("/sub/chat", function(msg) {
                onMessage(msg.body);
            });

            function send(){
                let msg = document.getElementById("msg");
                stompClient.send("/pub/chat", {}, username + ": " + msg.value);
                msg.value = '';
            }

            // 채팅 메시지 처리 함수
            function onMessage(data) {
                console.log("Received message:", data); // 수신된 메시지를 콘솔에 출력
                // ":"를 기준으로 메시지를 분리하여 sender와 message를 얻습니다.
                var arr = data.split(":");
                var sender = arr[0];
                var message = arr.slice(1).join(":").trim(); // ":" 이후의 모든 문자열을 message로 설정하며, 앞뒤 공백을 제거합니다.
                console.log(" sender 추출:", sender);
                console.log(" message 추출:", message);

                // 게임 요청이 있는지 확인
                if (message === "게임 요청") {
                    if (sender !== username) {
                        // 상대방에게만 모달 창
                        console.log(" message 추출:", message);
                        $("#gameRequestModal").modal("show");
                        // 모달 창에 요청 내용 표시
                        $("#gameRequestText").text(sender + "님이 게임을 요청했습니다. 수락하시겠습니까?");
                        // 수락 버튼 클릭 시 이벤트
                        $("#acceptGame").on("click", function() {
                            // 서버로 수락 메시지 전송
                            stompClient.send("/pub/chat", {}, "게임 수락");
                            console.log(" 게임을 수락했습니다" );
                            // 게임 시작 로직 추가
                            gameAccepted = true; // 게임 수락 상태로 변경
                            $("#gameRequestModal").modal("hide"); // 모달 창 숨기기
                            $("#msgArea").hide(); // 채팅 입력 숨기기
                        });
                        // 거절 버튼 클릭 시 이벤트
                        $("#rejectGame").on("click", function() {
                            // 서버로 거절 메시지 전송
                            stompClient.send("/pub/chat", {}, "게임 거절");
                            console.log(" 게임을 거절했습니다" );
                            $("#gameRequestModal").modal("hide"); // 모달 창 숨기기
                        });
                    }
                } else if (message.startsWith("게임 제스처 선택:")) {
                    // 게임 제안자가 가위, 바위, 보 중 하나를 선택했을 때의 처리
                    var gesture = message.split(":")[1].trim();
                    // 게임 제안자에게 선택한 제스처를 표시하거나 다른 처리를 수행
                } else if (data == "게임 수락") {
                        $("#gameChoices").show();
                            $("#msgArea").hide();
                             gameAccepted = true
                } else {
                    // 일반 채팅 메시지 처리
                    var str = "<div class='col-6'>";
                    str += "<div class='alert alert-secondary'>";
                    str += "<b>" + sender + ": " + message + "</b>";
                    str += "</div></div>";
                    $("#msgArea").append(str);
                }
            }
        }, function(error) {
            console.log('WebSocket 연결 실패:', error);
            // 연결 실패 시 추가 처리 로직을 넣으세요.
        });
    }
});