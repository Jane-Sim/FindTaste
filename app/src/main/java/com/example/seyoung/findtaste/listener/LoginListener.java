package com.example.seyoung.findtaste.listener;

import com.example.seyoung.findtaste.model.Account;

/**
 * Created by seyoung on 2017-10-13.
 * 사용자가 로그인을 했을 때 성공적으로 햇는 지 에러가 났는 지 상태를 반환해주는 인터페이스
 */

public interface LoginListener {
    void getDataSuccess(Account account);
    void getMessageError(Exception e);
}
