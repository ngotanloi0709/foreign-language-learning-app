package com.ngtnl1.foreign_language_learning_app.service;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthValidationService {
    @Inject
    public AuthValidationService() {
    }

    public boolean isEmailValid(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    public boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    public boolean isUsernameValid(String username) {
        String usernameRegex = "^[a-zA-ZÁÀẢẠÃĂẮẰẲẶẴÂẤẦẨẬẪáàảạãăắằẳặẵâấầẩậẫÉÈẺẸẼÊẾỀỂỆỄéèẻẹẽêếềểệễÍÌỈỊĨíìỉịĩÓÒỎỌÕƠỚỜỞỢỠÔỐỒỔỘỖóòỏọõơớờởợỡôốồổộỗÚÙỦỤŨƯỨỪỬỰỮúùủụũưứừửựữ]+( [a-zA-ZÁÀẢẠÃĂẮẰẲẶẴÂẤẦẨẬẪáàảạãăắằẳặẵâấầẩậẫÉÈẺẸẼÊẾỀỂỆỄéèẻẹẽêếềểệễÍÌỈỊĨíìỉịĩÓÒỎỌÕƠỚỜỞỢỠÔỐỒỔỘỖóòỏọõơớờởợỡôốồổộỗÚÙỦỤŨƯỨỪỬỰỮúùủụũưứừửựữ]+)*$";
        return username.matches(usernameRegex);
    }


    public boolean isPhoneValid(String phone) {
        return phone.matches("^(?:\\d{9}|\\d{10})$");
    }

    public boolean isRepeatPasswordValid(String password, String repeatPassword) {
        return password.equals(repeatPassword);
    }
}
