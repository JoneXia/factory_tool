package com.petkit.matetool.ui.permission.view;


import com.petkit.matetool.ui.permission.mode.PermissionBean;

import java.util.List;

/**
 * Created by petkit on 16/5/11.
 */
public interface IPermissionDialogView {

    void startQueryPermission(List<PermissionBean> permissionBeanList);
    void initPermissionList(List<PermissionBean> permissionBeanList);
    boolean checkSelfPermissionContent(String permissionContent);
}
