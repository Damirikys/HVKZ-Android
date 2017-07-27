package org.hvkz.hvkz.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.hvkz.hvkz.HVKZApp;
import org.hvkz.hvkz.uapi.models.UAPIClient;
import org.hvkz.hvkz.uapi.models.entities.User;

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
}
