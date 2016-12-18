package su.elevenets.devicemanagerclient.managers;

import android.util.Log;
import rx.Observable;
import rx.Single;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import su.elevenets.devicemanagerclient.bus.BroadcastBus;
import su.elevenets.devicemanagerclient.bus.events.DeviceBootEvent;
import su.elevenets.devicemanagerclient.bus.events.NetworkChangedEvent;
import su.elevenets.devicemanagerclient.bus.events.NewFirebaseToken;
import su.elevenets.devicemanagerclient.bus.events.PingEvent;
import su.elevenets.devicemanagerclient.consts.Tags;
import su.elevenets.devicemanagerclient.managers.loc.Loc;
import su.elevenets.devicemanagerclient.managers.loc.LocManager;
import su.elevenets.devicemanagerclient.models.DeviceProfile;
import su.elevenets.devicemanagerclient.utils.Actions;


/**
 * Created by eugene.levenetc on 14/11/2016.
 */
public class DeviceProfileManagerImpl implements DeviceProfileManager {

	private static final String TAG = DeviceProfileManager.class.getSimpleName();

	private RestManager restManager;
	private AppManager appManager;
	private LocManager locManager;
	private BroadcastBus broadcastBus;
	private SchedulersManager schedulersManager;
	private Logger logger;

	public DeviceProfileManagerImpl(
			RestManager restManager,
			AppManager appManager,
			LocManager locManager,
			BroadcastBus broadcastBus,
			SchedulersManager schedulersManager,
	        Logger logger
	) {
		this.restManager = restManager;
		this.appManager = appManager;
		this.locManager = locManager;
		this.broadcastBus = broadcastBus;
		this.schedulersManager = schedulersManager;
		this.logger = logger;
	}

	@Override public Observable<Object> uploadDeviceProfile() {

		return getDeviceProfile().flatMap(new Func1<DeviceProfile, Observable<Object>>() {
			@Override
			public Observable<Object> call(DeviceProfile device) {
				return restManager.getApi().postDevice(device);
			}
		}).doOnError(new Action1<Throwable>() {
			@Override public void call(Throwable throwable) {
				throwable.printStackTrace();
			}
		}).doOnNext(o -> updateLocation());
	}

	@Override public Single<Object> updateOnlineState() {
		return restManager.getApi().updateOnlineState(appManager.getDeviceId(), true);
	}

	@Override public Observable<DeviceProfile> getDeviceProfile() {

		//TODO: fix wifi network extra quotes

		return appManager.getGcmToken().map(token -> {
			DeviceProfile device = new DeviceProfile();
			device.pushToken = token;
			device.deviceId = appManager.getDeviceId();
			device.manufacturer = appManager.getManufacturer();
			device.model = appManager.getModel();
			device.osVersion = appManager.osVersion();
			device.batteryLevel = appManager.batteryLevel();
			device.hasBluetooth = appManager.hasBluetooth();
			device.hasBluetoothLowEnergy = appManager.hasBluetoothLowEnergy();
			device.hasFingerprintScanner = appManager.hasFingerprintScanner();
			device.hasNfc = appManager.hasNfc();
			device.wifiSSID = appManager.getWiFiSSID();
			device.screenWidth = appManager.getScreenWidth();
			device.screenHeight = appManager.getScreenHeight();
			device.screenSize = appManager.getScreenSize();
			return device;
		});
	}

	@Override public void subscribeOnDeviceEvents() {

		//TODO: add check if bound

		broadcastBus
				.subscribeOn(DeviceBootEvent.class)
				.filter(deviceBootEvent -> appManager.isConnectedToNetwork())
				.flatMap(event -> uploadDeviceProfile().onErrorResumeNext(throwable -> Observable.empty()))
				.subscribeOn(schedulersManager.background())
				.observeOn(schedulersManager.ui())
				.doOnUnsubscribe(new Action0() {
					@Override public void call() {
						logger.error(TAG, "unsubscribed from DeviceBootEvent");
					}
				})
				.subscribe(Actions.empty(), Actions.error());

		broadcastBus
				.subscribeOn(NetworkChangedEvent.class)
				.filter(event -> appManager.isConnectedToNetwork())
				.flatMap(event -> uploadDeviceProfile().onErrorResumeNext(throwable -> Observable.empty()))
				.subscribeOn(schedulersManager.background())
				.observeOn(schedulersManager.ui())
				.doOnUnsubscribe(new Action0() {
					@Override public void call() {
						logger.error(TAG, "unsubscribed from NetworkChangedEvent");
					}
				})
				.subscribe(Actions.empty(), Actions.error());

		broadcastBus
				.subscribeOn(PingEvent.class)
				.filter(event -> appManager.isConnectedToNetwork())
				.flatMap(event -> uploadDeviceProfile().onErrorResumeNext(throwable -> Observable.empty()))
				.subscribeOn(schedulersManager.background())
				.observeOn(schedulersManager.ui())
				.doOnUnsubscribe(new Action0() {
					@Override public void call() {
						logger.error(TAG, "unsubscribed from PingEvent");
					}
				})
				.subscribe(Actions.empty(), Actions.error());

		broadcastBus
				.subscribeOn(NewFirebaseToken.class)
				.filter(event -> appManager.isConnectedToNetwork())
				.flatMap(event -> uploadDeviceProfile().onErrorResumeNext(throwable -> Observable.empty()))
				.subscribeOn(schedulersManager.background())
				.observeOn(schedulersManager.ui())
				.doOnUnsubscribe(new Action0() {
					@Override public void call() {
						logger.error(TAG, "unsubscribed from NetworkChangedEvent");
					}
				})
				.subscribe(Actions.empty(), Actions.error());
	}


	private Func1<Loc, Single<Object>> sendLocation() {
		return location -> restManager.getApi().updateLocation(
				appManager.getDeviceId(),
				location.getLat(),
				location.getLon()
		).subscribeOn(schedulersManager.background());
	}

	private void updateLocation() {
		locManager
				.getLocation()
				.subscribeOn(schedulersManager.ui())
				.flatMap(sendLocation())
				.subscribe(Actions.empty(), Actions.error());
	}
}
