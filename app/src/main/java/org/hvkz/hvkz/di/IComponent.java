package org.hvkz.hvkz.di;

import org.hvkz.hvkz.auth.AuthActivity;
import org.hvkz.hvkz.auth.AuthViewHandler;
import org.hvkz.hvkz.database.GalleryStorage;
import org.hvkz.hvkz.database.MessagesStorage;
import org.hvkz.hvkz.firebase.db.PhotosStorage;
import org.hvkz.hvkz.modules.NavigationActivity;
import org.hvkz.hvkz.modules.chats.ChatsViewHandler;
import org.hvkz.hvkz.modules.chats.window.ChatDisposer;
import org.hvkz.hvkz.modules.chats.window.ui.ChatWindowPresenter;
import org.hvkz.hvkz.modules.menu.MenuViewHandler;
import org.hvkz.hvkz.modules.moderate.GroupEditorPresenter;
import org.hvkz.hvkz.modules.profile.ProfileViewHandler;
import org.hvkz.hvkz.sync.SyncInteractor;
import org.hvkz.hvkz.sync.SyncPresenter;
import org.hvkz.hvkz.xmpp.ConnectionService;
import org.hvkz.hvkz.xmpp.config.XMPPCredentials;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = DependencyProvider.class)
public interface IComponent
{
    void inject(AuthActivity activity);

    void inject(ChatsViewHandler viewHandler);

    void inject(ChatDisposer disposer);

    void inject(AuthViewHandler authViewHandler);

    void inject(MenuViewHandler viewHandler);

    void inject(GroupEditorPresenter groupEditorPresenter);

    void inject(ConnectionService service);

    void inject(MessagesStorage storage);

    void inject(ChatWindowPresenter chatWindowPresenter);

    void inject(GalleryStorage storage);

    void inject(ProfileViewHandler viewHandler);

    void inject(PhotosStorage photosStorage);

    void inject(NavigationActivity activity);

    void inject(SyncInteractor.SyncRequest syncRequest);

    void inject(SyncPresenter syncCallback);

    void inject(XMPPCredentials credentials);
}
