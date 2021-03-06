package su.elevenets.mobilemanager.di;

import android.content.Context;
import su.elevenets.mobilemanager.di.components.AppComponent;
import su.elevenets.mobilemanager.di.components.DaggerAppComponent;
import su.elevenets.mobilemanager.di.modules.AppModule;
import su.elevenets.mobilemanager.di.modules.RestModule;
import su.elevenets.mobilemanager.managers.AppManager;
import su.elevenets.mobilemanager.managers.KeyValueManager;
import su.elevenets.mobilemanager.managers.RestManager;
import su.elevenets.mobilemanager.managers.loc.LocManager;

/**
 * Created by eleven on 27/08/2016.
 */
public class DIHelper {

	private static AppComponent appComponent;
	private static RestManager restManager;
	private static LocManager locManager;
	private static KeyValueManager keyValueManager;
	private static AppManager appManager;

	public static void init(Context appContext) {
		final AppModule appModule = new AppModule(appContext);
		final RestModule restModule = new RestModule(restManager);

		appModule.setLocManager(locManager);
		appModule.setKeyValueManager(keyValueManager);

		appComponent = DaggerAppComponent.builder()
				.appModule(appModule)
				.restModule(restModule)
				.build();
	}

	public static AppComponent getAppComponent() {
		return appComponent;
	}

	public static void setRestManager(RestManager restManager) {
		DIHelper.restManager = restManager;
	}

	public static void setLocManager(LocManager locManager) {
		DIHelper.locManager = locManager;
	}

	public static void setKeyValueManager(KeyValueManager keyValueManager) {
		DIHelper.keyValueManager = keyValueManager;
	}

	public static void setAppManager(AppManager appManager) {
		DIHelper.appManager = appManager;
	}

}
