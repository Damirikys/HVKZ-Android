package org.hvkz.hvkz.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.firebase.db.GroupsStorage;
import org.hvkz.hvkz.firebase.db.MenuStorage;
import org.hvkz.hvkz.firebase.db.OptionsStorage;
import org.hvkz.hvkz.firebase.db.PhotosStorage;
import org.hvkz.hvkz.firebase.db.UsersStorage;
import org.hvkz.hvkz.uapi.UAPIClient;
import org.hvkz.hvkz.uapi.User;
import org.hvkz.hvkz.utils.controllers.NotificationController;

import dagger.Module;
import dagger.Provides;

@Module
public class DependencyProvider
{
    private HVKZApp app;

    public DependencyProvider(HVKZApp app) {
        this.app = app;
    }

    @Provides
    public FirebaseUser provideUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Provides
    public DatabaseReference provideDatabase() {
        return FirebaseDatabase.getInstance().getReference();
    }

    @Provides
    public UAPIClient provideUAPIClient() {
        return app.getUAPIclient();
    }

    @Provides
    public User provideUAPIUser() {
        return app.getCurrentUser();
    }

    @Provides
    public GroupsStorage provideGroupsStorage() {
        return app.getGroupsStorage();
    }

    @Provides
    public UsersStorage provideUserStorage() {
        return app.getUsersStorage();
    }

    @Provides
    public PhotosStorage providePhotoStorage() {
        return app.getPhotosStorage();
    }

    @Provides
    public OptionsStorage provideOptionsStorage() {
        return app.getOptionsStorage();
    }
    @Provides
    public GalleryStorage provideGalleryStorage() {
        return app.getGalleryStorage();
    }

    @Provides
    public MenuStorage provideMenuStorage() {
        return app.getMenuStorage();
    }

    @Provides
    public MessagesStorage provideMessagesStorage() {
        return app.getMessagesStorage();
    }

    @Provides
    public NotificationController provideNotificationController() {
        return app.getNotificationService();
    }
}
