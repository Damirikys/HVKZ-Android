package org.hvkz.hvkz.di;

import org.hvkz.hvkz.auth.LoginActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = DependencyProvider.class)
public interface IComponent
{
    void inject(LoginActivity activity);
}
