package jsipola.apptwo;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import dji.sdk.Products.DJIAircraft;
import dji.sdk.SDKManager.DJISDKManager;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseComponent.DJIComponentListener;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIBaseProduct.DJIBaseProductListener;
import dji.sdk.base.DJIBaseProduct.DJIComponentKey;
import dji.sdk.base.DJISDKError;

/**
 *  Reference: https://github.com/DJI-Mobile-SDK/Android-FPVDemo/blob/master/FPV-Demo/FPV-Demo-AS/app/src/main/java/com/dji/fpvtutorial/FPVTutorialActivity.java
 */

public class Tutorial extends Application{

    private static final String TAG = Tutorial.class.getName();

    public static final String FLAG_CONNECTION_CHANGE = "com_example_fpv_tutorial_connection_change";

    private static DJIBaseProduct mProduct;

    private Handler mHandler;

    /**
     * This function is used to get the instance of DJIBaseProduct.
     * If no product is connected, it returns null.
     */
    public static synchronized DJIBaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getDJIProduct();
        }
        return mProduct;
    }

    // reference : http://forum.dev.dji.com/forum.php?mod=viewthread&tid=32075&extra=page%3D1%26filter%3Dtypeid%26typeid%3D307
    public static boolean isAircraftConnected() {
        DJIBaseProduct product = getProductInstance();
        return product != null && product instanceof DJIAircraft;
    }

    public static synchronized DJIAircraft getAircraftInstance() {
        if (!isAircraftConnected()) return null;
        return (DJIAircraft) getProductInstance();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        //This is used to start SDK services and initiate SDK.
        DJISDKManager.getInstance().initSDKManager(this, mDJISDKManagerCallback);
    }

    /**
     * When starting SDK services, an instance of interface DJISDKManager.DJISDKManagerCallback will be used to listen to
     * the SDK Registration result and the product changing.
     */
    private DJISDKManager.DJISDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.DJISDKManagerCallback() {

        //Listens to the SDK registration result
        @Override
        public void onGetRegisteredResult(DJISDKError error) {
            if(error == DJISDKError.REGISTRATION_SUCCESS) {
                //DJISDKManager.getInstance().startConnectionToProduct();
                boolean conne = DJISDKManager.getInstance().startConnectionToProduct();
                Log.e("TAG", "Connection to Product: "+String.valueOf(conne));
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "register sdk fails, check network is available", Toast.LENGTH_LONG).show();
                    }
                });

            }
            Log.e("TAG", error.getDescription());
        }

        //Listens to the connected product changing, including two parts, component changing or product connection changing.
        @Override
        public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct) {

            mProduct = newProduct;
            if(mProduct != null) {
                mProduct.setDJIBaseProductListener(mDJIBaseProductListener);
            }

            notifyStatusChange();
        }
    };

    private DJIBaseProductListener mDJIBaseProductListener = new DJIBaseProductListener() {

        @Override
        public void onComponentChange(DJIComponentKey key, DJIBaseComponent oldComponent, DJIBaseComponent newComponent) {

            if(newComponent != null) {
                newComponent.setDJIComponentListener(mDJIComponentListener);
            }
            notifyStatusChange();
        }

        @Override
        public void onProductConnectivityChanged(boolean isConnected) {

            notifyStatusChange();
        }

    };

    private DJIComponentListener mDJIComponentListener = new DJIComponentListener() {

        @Override
        public void onComponentConnectivityChanged(boolean isConnected) {
            notifyStatusChange();
        }

    };

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

}