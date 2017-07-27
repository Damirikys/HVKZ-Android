package org.hvkz.hvkz;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;

import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.di.DaggerIComponent;
import org.hvkz.hvkz.di.DependencyProvider;
import org.hvkz.hvkz.di.IComponent;
import org.hvkz.hvkz.firebase.db.MenuStorage;
import org.hvkz.hvkz.firebase.db.groups.GroupsStorage;
import org.hvkz.hvkz.firebase.db.photos.PhotosStorage;
import org.hvkz.hvkz.firebase.db.users.UsersStorage;
import org.hvkz.hvkz.interfaces.Callback;
import org.hvkz.hvkz.uapi.models.UAPIClient;
import org.hvkz.hvkz.uapi.models.entities.User;
import org.hvkz.hvkz.uapi.models.entities.UserProfile;
import org.hvkz.hvkz.utils.serialize.JSONFactory;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.LocalBinder;
import org.hvkz.hvkz.xmpp.notification_service.NotificationService;

public class HVKZApp extends Application
{
    private static final String USER_PREFERENCES = "user_pref";

    private SharedPreferences preferences;
    private IComponent component;
    private User currentUser;

    private UAPIClient uapiClient;

    private NotificationService notificationService;
    private MessagesStorage messagesStorage;
    private GalleryStorage galleryStorage;
    private GroupsStorage groupsStorage;
    private PhotosStorage photosStorage;
    private UsersStorage usersStorage;
    private MenuStorage menuStorage;

    private LocalBinder<ConnectionService> serviceBinder;
    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            serviceBinder = (LocalBinder<ConnectionService>) binder;
            serviceBinder.getService().tryConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerIComponent
                .builder()
                .dependencyProvider(new DependencyProvider(this))
                .build();

        preferences = getSharedPreferences(USER_PREFERENCES, MODE_PRIVATE);
        String userCache = preferences.getString(USER_PREFERENCES, "");
        if (!userCache.isEmpty()) {
            currentUser = JSONFactory.fromJson(preferences.getString(USER_PREFERENCES, ""), UserProfile.class);
        }

        uapiClient = new UAPIClient();

        notificationService = new NotificationService(this);
        messagesStorage = new MessagesStorage(this);
        galleryStorage = new GalleryStorage(this);
        groupsStorage = new GroupsStorage(this);
        photosStorage = new PhotosStorage(this);
        usersStorage = new UsersStorage(this);
        menuStorage = new MenuStorage(this);

        menuStorage.getAllFromRemote(x -> {});
    }

    /* Storage getters */
    public MessagesStorage getMessagesStorage() {
        return messagesStorage;
    }

    public GroupsStorage getGroupsStorage() {
        return groupsStorage;
    }

    public PhotosStorage getPhotosStorage() {
        return photosStorage;
    }

    public GalleryStorage getGalleryStorage() {
        return galleryStorage;
    }

    public UsersStorage getUsersStorage() {
        return usersStorage;
    }

    public MenuStorage getMenuStorage() {
        return menuStorage;
    }

    /* ---------------- */

    /* User options */
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser = user;
        preferences.edit()
                .putString(USER_PREFERENCES, JSONFactory.toJson(user))
                .apply();

        usersStorage.update(currentUser);

        Intent serviceIntent = new Intent(this, ConnectionService.class);
        if (!isMyServiceRunning(ConnectionService.class))
            startService(serviceIntent);

        bindService(serviceIntent, serviceConnection,
                Context.BIND_ABOVE_CLIENT |
                Context.BIND_AUTO_CREATE |
                Context.BIND_IMPORTANT
        );
    }
    /* ------------ */

    /* Services */
    public void bindConnectionService(Callback<ConnectionService> callback) {
        if (serviceBinder == null) {
            bindService(new Intent(this, ConnectionService.class), serviceConnection,
                    Context.BIND_ABOVE_CLIENT |
                            Context.BIND_AUTO_CREATE |
                            Context.BIND_IMPORTANT
            );

            new Thread(() -> {
                while (true) {
                    if (serviceBinder != null) {
                        callback.call(serviceBinder.getService());
                        break;
                    }
                }
            }).start();
        } else {
            callback.call(serviceBinder.getService());
        }
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public UAPIClient getUAPIclient() {
        return uapiClient;
    }

    public boolean isSynced() {
        return currentUser != null;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
    /* -------- */

    public IComponent component(){
        return component;
    }
}
