package org.hvkz.hvkz.di;

import org.hvkz.hvkz.auth.AuthActivity;
import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.firebase.db.photos.PhotosStorage;
import org.hvkz.hvkz.modules.MainActivity;
import org.hvkz.hvkz.modules.chats.window.ChatWindowPresenter;
import org.hvkz.hvkz.modules.profile.ProfileViewHandler;
import org.hvkz.hvkz.sync.SyncInteractor;
import org.hvkz.hvkz.sync.SyncPresenter;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.XMPPCredentials;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = DependencyProvider.class)
public interface IComponent
{
    void inject(AuthActivity activity);

    void inject(ConnectionService service);

    void inject(MessagesStorage storage);

    void inject(ChatWindowPresenter chatWindowPresenter);

    void inject(GalleryStorage storage);

    void inject(ProfileViewHandler viewHandler);

    void inject(PhotosStorage photosStorage);

    void inject(MainActivity activity);

    void inject(SyncInteractor.SyncRequest syncRequest);

    void inject(SyncPresenter syncCallback);

    void inject(XMPPCredentials credentials);
}
