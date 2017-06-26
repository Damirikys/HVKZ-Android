package org.hvkz.hvkz.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dagger.Module;
import dagger.Provides;

@Module
public class DependencyProvider
{
    @Provides
    public FirebaseUser provideUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Provides
    public DatabaseReference provideDatabase() {
        return FirebaseDatabase.getInstance().getReference();
    }
}
