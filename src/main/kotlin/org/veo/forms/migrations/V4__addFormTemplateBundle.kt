/*
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

@Suppress("ClassName")
class V4__addFormTemplateBundle : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().use {
            it.execute(
                """
                create table form_template_bundle (
                    id uuid not null,
                    domain_template_id uuid not null,
                    templates jsonb not null,
                    version varchar(255) not null,
                    primary key (id),
                    unique(domain_template_id, version)
                );
                
                alter table domain
                    drop column domain_template_version,
                    add column form_template_bundle_id uuid,
                    add constraint FK_form_template_bundle_id
                        foreign key (form_template_bundle_id)
                        references form_template_bundle;

                alter table form 
                    add column form_template_version varchar(255);

                """,
            )
        }
    }
}
