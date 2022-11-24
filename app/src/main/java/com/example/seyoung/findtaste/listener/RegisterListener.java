package com.example.seyoung.findtaste.listener;

import com.example.seyoung.findtaste.model.Account;

/**
 * Created by seyoung on 2017-10-13.
 * 사용자가 회원가입한 값이 정상적으로 처리됬는 지 아닌 지 알려준다
 */

public interface RegisterListener {
    void getDataSuccess(Account account);
    void getMessageError(Exception e);
}
