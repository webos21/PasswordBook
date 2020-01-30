import React from 'react';
import { List, Datagrid, TextField, EmailField, UrlField } from 'react-admin';

export const PasswordList = props => (
    <List {...props}>
        <Datagrid rowClick="edit">
            <TextField source="id" />
            <TextField source="name" />
            <EmailField source="email" />
            <TextField source="phone" />
            <TextField source="website" />
            <UrlField source="website" />
            <TextField source="company.name" />
        </Datagrid>
    </List>
);
