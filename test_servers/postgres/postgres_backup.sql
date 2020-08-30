--
-- PostgreSQL database dump
--

-- Dumped from database version 12.3 (Debian 12.3-1.pgdg100+1)
-- Dumped by pg_dump version 12.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE IF EXISTS pickaxe_dev;
--
-- Name: pickaxe_dev; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE pickaxe_dev WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.utf8' LC_CTYPE = 'en_US.utf8';


ALTER DATABASE pickaxe_dev OWNER TO postgres;

\connect pickaxe_dev

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: games; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.games (
    game character varying NOT NULL,
    week character varying NOT NULL,
    gametime timestamp with time zone,
    final boolean DEFAULT false NOT NULL,
    result character varying,
    spread numeric,
    id uuid
);


ALTER TABLE public.games OWNER TO postgres;

--
-- Name: userpicks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.userpicks (
    name character varying NOT NULL,
    week character varying NOT NULL,
    game character varying NOT NULL,
    pick character varying
);


ALTER TABLE public.userpicks OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    name character varying NOT NULL,
    active boolean DEFAULT true,
    email character varying,
    id integer NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: weeks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.weeks (
    name character varying NOT NULL,
    week integer NOT NULL,
    week_type character varying DEFAULT 'REG'::character varying NOT NULL,
    week_order integer NOT NULL
);


ALTER TABLE public.weeks OWNER TO postgres;

--
-- Name: weeks_week_order_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.weeks_week_order_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.weeks_week_order_seq OWNER TO postgres;

--
-- Name: weeks_week_order_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.weeks_week_order_seq OWNED BY public.weeks.week_order;


--
-- Name: weeks week_order; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.weeks ALTER COLUMN week_order SET DEFAULT nextval('public.weeks_week_order_seq'::regclass);


--
-- Data for Name: games; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.games (game, week, gametime, final, result, spread, id) FROM stdin;
GB@CHI	Week 0	\N	f	CHI	\N	\N
BUF@NE	Week 0	\N	f		\N	\N
SEA@PHI	Week 0	\N	f	SEA	\N	\N
NE@TB	Week 1	\N	f	NE	-14	a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11
\.


--
-- Data for Name: userpicks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.userpicks (name, week, game, pick) FROM stdin;
Seamus	Week 0	GB@CHI	CHI
Sereres	Week 0	SEA@PHI	PHI
Sereres	Week 1	NE@TB	ne
Seamus	Week 1	NE@TB	
Seamus	Week 0	SEA@PHI	SEA
RNG	Week 1	NE@TB	
Sereres	Week 0	GB@CHI	CHI
RNG	Week 0	GB@CHI	CHI
Vegas	Week 0	GB@CHI	CHI
RNG	Week 0	SEA@PHI	SEA
Vegas	Week 0	SEA@PHI	SEA
Vegas	Week 0	BUF@NE	BUF
Sereres	Week 0	BUF@NE	BUF
Seamus	Week 0	BUF@NE	BUF
RNG	Week 0	BUF@NE	BUF
Vegas	Week 1	NE@TB	TB
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (name, active, email, id) FROM stdin;
Seamus	t	\N	1
Vegas	t	\N	99
Sereres	t	\N	2
RNG	t	\N	98
\.


--
-- Data for Name: weeks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.weeks (name, week, week_type, week_order) FROM stdin;
Week 0	0	REG	1
Week 1	1	REG	2
Week 6	6	REG	6
\.


--
-- Name: weeks_week_order_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.weeks_week_order_seq', 2, true);


--
-- Name: games games_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.games
    ADD CONSTRAINT games_pk PRIMARY KEY (game, week);


--
-- Name: userpicks userpicks_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userpicks
    ADD CONSTRAINT userpicks_pk PRIMARY KEY (name, week, game);


--
-- Name: users users_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk PRIMARY KEY (name);


--
-- Name: weeks weeks_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.weeks
    ADD CONSTRAINT weeks_pk PRIMARY KEY (name);


--
-- Name: users_id_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX users_id_uindex ON public.users USING btree (id);


--
-- Name: users_name_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX users_name_uindex ON public.users USING btree (name);


--
-- Name: weeks_week_order_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX weeks_week_order_uindex ON public.weeks USING btree (week_order);


--
-- Name: weeks_week_uindex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX weeks_week_uindex ON public.weeks USING btree (name);


--
-- Name: games games_weeks_week_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.games
    ADD CONSTRAINT games_weeks_week_fk FOREIGN KEY (week) REFERENCES public.weeks(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: userpicks userpicks_games_game_week_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userpicks
    ADD CONSTRAINT userpicks_games_game_week_fk FOREIGN KEY (game, week) REFERENCES public.games(game, week) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: userpicks userpicks_users_name_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.userpicks
    ADD CONSTRAINT userpicks_users_name_fk FOREIGN KEY (name) REFERENCES public.users(name) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- PostgreSQL database dump complete
--

