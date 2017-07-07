package org.hvkz.hvkz.di;

import org.hvkz.hvkz.auth.AuthActivity;
import org.hvkz.hvkz.db.firebase.PhotosDb;
import org.hvkz.hvkz.modules.home.MainActivity;
import org.hvkz.hvkz.modules.home.MainViewHandler;
import org.hvkz.hvkz.sync.SyncInteractor;
import org.hvkz.hvkz.sync.SyncPresenter;
import org.hvkz.hvkz.uapi.models.entities.UAPIUser;
import org.hvkz.hvkz.xmpp.XMPPCredentials;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = DependencyProvider.class)
public interface IComponent
{
    void inject(AuthActivity activity);

    void inject(MainViewHandler viewHandler);

    void inject(PhotosDb photosDb);

    void inject(MainActivity activity);

    void inject(SyncInteractor.SyncRequest syncRequest);

    void inject(SyncPresenter syncCallback);

    void inject(UAPIUser uapiUser);

    void inject(XMPPCredentials credentials);
}
