package org.hvkz.hvkz.di;

import org.hvkz.hvkz.MainActivity;
import org.hvkz.hvkz.auth.AuthActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = DependencyProvider.class)
public interface IComponent
{
    void inject(AuthActivity activity);

    void inject(MainActivity activity);
}
