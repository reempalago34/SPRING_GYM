package co.sena.adso.fitnation.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Estrategia de nombres que respeta los identificadores camelCase del
 * esquema de Flask.
 *
 * Por qué hace falta:
 *  - PostgreSQL minusculiza cualquier identificador escrito SIN comillas,
 *    así que "idUser" sin comillas se convierte en "iduser".
 *  - SQLAlchemy (Flask) emite esos nombres entre comillas, por lo que en
 *    producción la columna se llama exactamente "idUser".
 *  - Hibernate por defecto genera el SQL sin comillas -> mismatch.
 *
 * Esta estrategia entrecomilla cualquier nombre que contenga mayúsculas,
 * que es exactamente la condición que SQLAlchemy usa para decidir si
 * quotear o no.
 */
public class MixedCasePhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment context) {
        return quoteIfNeeded(logicalName);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment context) {
        return quoteIfNeeded(logicalName);
    }

    private Identifier quoteIfNeeded(Identifier logicalName) {
        if (logicalName == null) {
            return null;
        }
        boolean tieneMayusculas = logicalName.getText().chars().anyMatch(Character::isUpperCase);
        return tieneMayusculas ? Identifier.quote(logicalName) : logicalName;
    }
}
