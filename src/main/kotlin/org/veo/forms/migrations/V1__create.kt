/**
 * verinice.veo forms
 * Copyright (C) 2021  Jonas Jordan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.forms.migrations

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

class V1__create : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().use {
            it.execute(
                """
                create table domain (
                   id uuid not null,
                    client_id uuid,
                    domain_template_id uuid,
                    domain_template_version varchar(255),
                    primary key (id)
                );
            
                create table form (
                   id uuid not null,
                    content jsonb,
                    form_template_id uuid,
                    model_type int4,
                    name jsonb,
                    sub_type varchar(255),
                    translation jsonb,
                    domain_id uuid not null,
                    primary key (id)
                );
            
                alter table form
                   add constraint FK_domain_id
                   foreign key (domain_id)
                   references domain;
                """,
            )
        }
    }
}
