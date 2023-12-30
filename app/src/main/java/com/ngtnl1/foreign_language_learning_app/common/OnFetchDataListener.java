package com.ngtnl1.foreign_language_learning_app.common;

import com.ngtnl1.foreign_language_learning_app.model.APIResponse;

public interface OnFetchDataListener {
    void OnFetchData(APIResponse apiResponse, String message);
    void OnError(String message);
}
