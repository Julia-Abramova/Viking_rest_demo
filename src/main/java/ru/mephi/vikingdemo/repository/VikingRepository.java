package ru.mephi.vikingdemo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.VikingEntity;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class VikingRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<VikingEntity> vikingRowMapper = (rs, rowNum) ->
            new VikingEntity(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getInt("height_cm"),
                    HairColor.valueOf(rs.getString("hair_color")),
                    BeardStyle.valueOf(rs.getString("beard_style")),
                    rs.getString("description")
            );

    public VikingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<VikingEntity> findAll() {
        String sql = """
                select id, name, age, height_cm, hair_color, beard_style, description
                from vikings
                order by id
                """;

        return jdbcTemplate.query(sql, vikingRowMapper);
    }

    public Optional<VikingEntity> findById(int id) {
        String sql = """
                select id, name, age, height_cm, hair_color, beard_style, description
                from vikings
                where id = ?
                """;

        List<VikingEntity> result = jdbcTemplate.query(sql, vikingRowMapper, id);

        return result.stream().findFirst();
    }

    public Integer save(VikingEntity viking) {
        String sql = """
                insert into vikings(name, age, height_cm, hair_color, beard_style, description)
                values (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, viking.name());
            ps.setInt(2, viking.age());
            ps.setInt(3, viking.heightCm());
            ps.setString(4, viking.hairColor().name());
            ps.setString(5, viking.beardStyle().name());
            ps.setString(6, viking.description());

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException("Не удалось получить id созданного викинга");
        }

        return key.intValue();
    }

    public boolean update(VikingEntity viking) {
        String sql = """
                update vikings
                set name = ?,
                    age = ?,
                    height_cm = ?,
                    hair_color = ?,
                    beard_style = ?,
                    description = ?
                where id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                viking.name(),
                viking.age(),
                viking.heightCm(),
                viking.hairColor().name(),
                viking.beardStyle().name(),
                viking.description(),
                viking.id()
        );

        return updatedRows > 0;
    }

    public boolean deleteById(int id) {
        String sql = "delete from vikings where id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public void deleteAll() {
        jdbcTemplate.update("delete from vikings");
    }
}
