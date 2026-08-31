document.addEventListener('DOMContentLoaded', function () {
    var form = document.querySelector('#signup-form');
    var userId = document.querySelector('#user-id');
    var checkUserIdButton = document.querySelector('#check-user-id');
    var userIdMessage = document.querySelector('#user-id-message');
    var password = document.querySelector('#password');
    var passwordConfirm = document.querySelector('#password-confirm');
    var passwordConfirmMessage = passwordConfirm.closest('.field').querySelector('.field-message');
    var phone = document.querySelector('#phone');
    var phoneMessage = document.querySelector('#phone-message');
    var phoneVerificationToken = document.querySelector('#phone-verification-token');
    var verificationArea = document.querySelector('#phone-verification');
    var phoneCode = document.querySelector('#phone-code');
    var phoneCodeMessage = document.querySelector('#phone-code-message');
    var requestPhoneCodeButton = document.querySelector('#request-phone-code');
    var verifyPhoneCodeButton = document.querySelector('#verify-phone-code');
    var timerElement = document.querySelector('#verification-timer');
    var postcode = document.querySelector('#postcode');
    var address = document.querySelector('#address');
    var addressDetail = document.querySelector('#address-detail');
    var addressMessage = document.querySelector('#address-message');
    var csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    var csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

    var userIdChecked = false;
    var checkedUserId = '';
    var verifiedPhone = '';
    var timerId;

    function setMessage(element, text, success) {
        if (!element) return;
        element.textContent = text || '';
        element.classList.toggle('success', success === true);
    }

    function postJson(url, body) {
        var headers = { 'Content-Type': 'application/json' };
        if (csrfTokenMeta && csrfHeaderMeta) {
            headers[csrfHeaderMeta.content] = csrfTokenMeta.content;
        }

        return fetch(url, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(body)
        }).then(function (response) {
            return response.json().catch(function () {
                return { success: false, message: '서버 응답을 처리할 수 없습니다.' };
            }).then(function (data) {
                if (!response.ok || data.success !== true) {
                    throw new Error(data.message || '요청 처리에 실패했습니다.');
                }
                return data;
            });
        });
    }

    function normalizePhone(value) {
        return (value || '').replace(/[^0-9]/g, '');
    }

    function clearPhoneVerification() {
        verifiedPhone = '';
        phoneVerificationToken.value = '';
        phone.classList.remove('phone-verified');
        verificationArea.classList.remove('is-verified');
        phone.readOnly = false;
        phoneCode.readOnly = false;
        requestPhoneCodeButton.disabled = false;
        verifyPhoneCodeButton.disabled = false;
        verifyPhoneCodeButton.textContent = '인증확인';
        setMessage(phoneCodeMessage, '');
    }

    function showVerifiedPhone(phoneNumber, verificationToken) {
        verifiedPhone = phoneNumber;
        phoneVerificationToken.value = verificationToken;

        clearInterval(timerId);
        timerElement.textContent = '인증 완료';

        phone.classList.remove('invalid');
        phone.classList.add('phone-verified');
        phone.readOnly = true;

        phoneCode.classList.remove('invalid');
        phoneCode.readOnly = true;

        verificationArea.classList.add('is-verified');
        requestPhoneCodeButton.disabled = true;
        verifyPhoneCodeButton.disabled = true;
        verifyPhoneCodeButton.textContent = '인증완료';

        setMessage(phoneMessage, '인증된 휴대전화 번호입니다.', true);
        setMessage(phoneCodeMessage, '✓ 휴대전화 인증이 완료되었습니다.', true);
    }

    function resetUserIdCheck() {
        userIdChecked = false;
        checkedUserId = '';
        userId.classList.remove('invalid');
        userId.classList.remove('id-available');
        setMessage(userIdMessage, '');
    }

    function showUnavailableUserId(message) {
        userIdChecked = false;
        checkedUserId = '';
        userId.classList.remove('id-available');
        userId.classList.add('invalid');
        setMessage(userIdMessage, message, false);
    }

    userId.addEventListener('input', function () {
        userId.value = userId.value.toLowerCase().replace(/[^a-z0-9]/g, '');
        resetUserIdCheck();
    });

    checkUserIdButton.addEventListener('click', function () {
        var empId = userId.value.trim();
        resetUserIdCheck();

        if (!userId.checkValidity()) {
            showUnavailableUserId('영문 소문자와 숫자로 4~20자를 입력해 주세요.');
            userId.focus();
            return;
        }

        fetch('/api/members/check-id?empId=' + encodeURIComponent(empId))
            .then(function (response) {
                if (!response.ok) throw new Error('아이디 확인 요청에 실패했습니다.');
                return response.json();
            })
            .then(function (result) {
                if (result.available === true) {
                    userIdChecked = true;
                    checkedUserId = empId;
                    userId.classList.remove('invalid');
                    userId.classList.add('id-available');
                    setMessage(userIdMessage, '사용 가능한 아이디입니다.', true);
                } else {
                    showUnavailableUserId('이미 사용 중인 아이디입니다.');
                }
            })
            .catch(function (error) {
                showUnavailableUserId(error.message);
            });
    });

    document.querySelectorAll('.password-toggle').forEach(function (button) {
        button.addEventListener('click', function () {
            var input = document.querySelector('#' + button.dataset.target);
            var show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            button.textContent = show ? '숨기기' : '보기';
        });
    });

    function validatePasswordMatch() {
        passwordConfirm.classList.remove('invalid', 'password-matched');

        if (!passwordConfirm.value) {
            passwordConfirm.setCustomValidity('');
            setMessage(passwordConfirmMessage, '');
            return false;
        }

        if (password.value !== passwordConfirm.value) {
            passwordConfirm.setCustomValidity('비밀번호가 일치하지 않습니다.');
            passwordConfirm.classList.add('invalid');
            setMessage(passwordConfirmMessage, '✕ 비밀번호가 일치하지 않습니다.');
            return false;
        }

        passwordConfirm.setCustomValidity('');

        if (!password.checkValidity()) {
            passwordConfirm.classList.add('invalid');
            setMessage(passwordConfirmMessage, '비밀번호는 8자 이상 입력해 주세요.');
            return false;
        }

        passwordConfirm.classList.add('password-matched');
        setMessage(passwordConfirmMessage, '✓ 비밀번호가 일치합니다.', true);
        return true;
    }

    password.addEventListener('input', validatePasswordMatch);
    passwordConfirm.addEventListener('input', validatePasswordMatch);

    phone.addEventListener('input', function () {
        phone.value = normalizePhone(phone.value);
        clearPhoneVerification();
        setMessage(phoneMessage, '');
    });

    function startTimer() {
        clearInterval(timerId);
        var remaining = 180;

        function render() {
            var minutes = String(Math.floor(remaining / 60)).padStart(2, '0');
            var seconds = String(remaining % 60).padStart(2, '0');
            timerElement.textContent = minutes + ':' + seconds;

            if (remaining <= 0) {
                clearInterval(timerId);
                phoneVerificationToken.value = '';
                setMessage(
                    phoneCodeMessage,
                    '인증번호가 만료되었습니다. 다시 요청해 주세요.'
                );
                return;
            }
            remaining -= 1;
        }

        render();
        timerId = setInterval(render, 1000);
    }

    requestPhoneCodeButton.addEventListener('click', function () {
        var phoneNumber = normalizePhone(phone.value);

        if (!phone.checkValidity()) {
            phone.classList.add('invalid');
            setMessage(phoneMessage, '올바른 휴대전화 번호를 입력해 주세요.');
            return;
        }

        clearPhoneVerification();
        requestPhoneCodeButton.disabled = true;
        setMessage(phoneMessage, '인증번호를 발송하고 있습니다.');

        postJson('/api/phone-verifications/request', { phone: phoneNumber })
            .then(function (result) {
                phone.classList.remove('invalid');
                verificationArea.hidden = false;
                phoneCode.required = true;
                phoneCode.value = '';
                phoneCode.focus();
                setMessage(phoneMessage, result.message, true);
                startTimer();
            })
            .catch(function (error) {
                setMessage(phoneMessage, error.message);
            })
            .finally(function () {
                requestPhoneCodeButton.disabled = false;
            });
    });

    phoneCode.addEventListener('input', function () {
        phoneCode.value = phoneCode.value.replace(/[^0-9]/g, '');
        phoneVerificationToken.value = '';
    });

    verifyPhoneCodeButton.addEventListener('click', function () {
        var phoneNumber = normalizePhone(phone.value);
        var code = phoneCode.value.trim();

        if (!code.match(/^[0-9]{6}$/)) {
            setMessage(phoneCodeMessage, '인증번호 6자리를 입력해 주세요.');
            return;
        }

        verifyPhoneCodeButton.disabled = true;
        setMessage(phoneCodeMessage, '인증번호를 확인하고 있습니다.');

        postJson('/api/phone-verifications/verify', {
            phone: phoneNumber,
            code: code
        })
            .then(function (result) {
                showVerifiedPhone(phoneNumber, result.verificationToken);
            })
            .catch(function (error) {
                phoneVerificationToken.value = '';
                phoneCode.classList.add('invalid');
                setMessage(phoneCodeMessage, error.message);
            })
            .finally(function () {
                if (!phoneVerificationToken.value) {
                    verifyPhoneCodeButton.disabled = false;
                }
            });
    });

    document.querySelector('#search-address').addEventListener('click', function () {
        if (!window.kakao || !window.kakao.Postcode) {
            setMessage(
                addressMessage,
                '주소 검색 서비스를 불러오지 못했습니다. 인터넷 연결을 확인해 주세요.'
            );
            return;
        }

        new window.kakao.Postcode({
            oncomplete: function (data) {
                var selectedAddress = data.userSelectedType === 'R'
                    ? data.roadAddress
                    : data.jibunAddress;
                var extraAddress = '';

                if (data.userSelectedType === 'R') {
                    if (data.bname && /[동로가]$/.test(data.bname)) {
                        extraAddress = data.bname;
                    }

                    if (data.buildingName && data.apartment === 'Y') {
                        extraAddress += extraAddress
                            ? ', ' + data.buildingName
                            : data.buildingName;
                    }
                }

                postcode.value = data.zonecode;
                address.value = selectedAddress
                    + (extraAddress ? ' (' + extraAddress + ')' : '');

                addressDetail.value = '';
                postcode.classList.remove('invalid');
                address.classList.remove('invalid');
                addressDetail.classList.remove('invalid');
                setMessage(addressMessage, '주소가 선택되었습니다. 상세주소를 입력해 주세요.', true);
                addressDetail.focus();
            }
        }).open();
    });

    form.addEventListener('submit', function (event) {
        var valid = true;

        form.querySelectorAll('input[required]').forEach(function (input) {
            if (!input.checkValidity()) {
                input.classList.add('invalid');
                valid = false;
            }
        });

        if (!userIdChecked || checkedUserId !== userId.value.trim()) {
            valid = false;
            showUnavailableUserId('현재 아이디의 중복확인을 진행해 주세요.');
        }

        if (!validatePasswordMatch()) {
            valid = false;
        }

        if (!phoneVerificationToken.value
                || verifiedPhone !== normalizePhone(phone.value)) {
            valid = false;
            setMessage(phoneCodeMessage, '휴대전화 인증을 완료해 주세요.');
        }

        if (!postcode.value || !address.value) {
            valid = false;
            setMessage(addressMessage, '주소 검색을 진행해 주세요.');
        }

        if (!valid) event.preventDefault();
    });
});
