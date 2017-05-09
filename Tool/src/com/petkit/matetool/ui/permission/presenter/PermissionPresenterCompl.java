package com.petkit.matetool.ui.permission.presenter;

import android.Manifest;
import com.petkit.matetool.R;
import com.petkit.matetool.ui.permission.mode.PermissionBean;
import com.petkit.matetool.ui.permission.view.IPermissionDialogView;

import java.util.ArrayList;

/**
 *
 *
 * Created by petkit on 16/5/11.
 */
public class PermissionPresenterCompl implements IPermissionPresenter {

    private IPermissionDialogView mIPermissionDialogView;

    private ArrayList<PermissionBean> mPermissionBeanList;

    public PermissionPresenterCompl(IPermissionDialogView IPermissionDialogView, ArrayList<PermissionBean> permissionBeans) {
        mIPermissionDialogView = IPermissionDialogView;
        mPermissionBeanList = permissionBeans;

        initPermissionList();

        mIPermissionDialogView.initPermissionList(mPermissionBeanList);
    }

    @Override
    public void startNext() {
        mIPermissionDialogView.startQueryPermission(mPermissionBeanList);
    }

    private void initPermissionList(){
        if(mPermissionBeanList == null || mPermissionBeanList.size() == 0){
            mPermissionBeanList = new ArrayList<>();
            if(!mIPermissionDialogView.checkSelfPermissionContent(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                mPermissionBeanList.add(new PermissionBean(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.Permission_storage, R.drawable.permission_storage));
            }

//            if(!mIPermissionDialogView.checkSelfPermissionContent(Manifest.permission.ACCESS_FINE_LOCATION)){
//                mPermissionBeanList.add(new PermissionBean(Manifest.permission.ACCESS_FINE_LOCATION, R.string.Permission_location, R.drawable.permission_location));
//            }
        }
    }



}
