package org.unicef.rapidreg;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.akaita.java.rxjava2debug.RxJava2Debug;
import com.facebook.stetho.Stetho;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.unicef.rapidreg.repository.impl.SQLCipherHelperImpl;
import org.unicef.rapidreg.injection.component.ApplicationComponent;
import org.unicef.rapidreg.injection.component.DaggerApplicationComponent;
import org.unicef.rapidreg.injection.module.ApplicationModule;

public class PrimeroApplication extends Application {
    private static Context context;
    private static AppRuntime appRuntime;

    ApplicationComponent applicationComponent;

    public static Context getAppContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        appRuntime = new AppRuntime(context);

        if (BuildConfig.DEBUG && isRoboUnitTest()) {
            Stetho.initializeWithDefaults(context);
            RxJava2Debug.enableRxJava2AssemblyTracking(new String[]{"org.unicef.rapidreg"});
        }

        PrimeroAppConfiguration.setInternalFilePath(context.getFilesDir().getPath() );
        initDB();
    }

    // TODO: need to realise get in progress Sychronization tasks
    public Object getSyncTask() {
        return null;
    }

    private void initDB() {
        FlowManager.init(new FlowConfig.Builder(this)
                .addDatabaseConfig(new DatabaseConfig.Builder(PrimeroDatabaseConfiguration.class)
                        .openHelper((databaseDefinition, helperListener) -> new SQLCipherHelperImpl(databaseDefinition, helperListener)).build())
                .build());
    }

    public static PrimeroApplication get(Context context) {
        return (PrimeroApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        if (applicationComponent == null) {
            applicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return applicationComponent;
    }

    public void setApplicationComponent(ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    private static boolean isRoboUnitTest() {
        return "robolectric".equals(Build.FINGERPRINT);
    }

    public static AppRuntime getAppRuntime() {
        return appRuntime;
    }
}
