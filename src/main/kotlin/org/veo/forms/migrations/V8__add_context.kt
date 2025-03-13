/**
 * verinice.veo forms
 * Copyright (C) 2025  Jonas Jordan
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
class V8__add_context : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().use {
            it.execute(
                """
                    alter table form 
                       add column context varchar(255) check (context in ('ElementDetails','RequirementImplementationControlView'));
                   update form set context = 'ElementDetails';
                   alter table form
                        alter column context set not null;
                    update form_template_bundle
                        set templates = (select jsonb_object_agg(key, value || '{
                          "context": "elementDetails"
                        }'::jsonb) from jsonb_each(templates));
                """,
            )
        }
    }
}
